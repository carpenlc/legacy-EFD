/*global Ext, EFD, StatusService */

Ext.ns('EFD.dashboard');

EFD.dashboard.Dashboard = Ext.extend(Ext.Panel, {

    height: 250,
    layout: 'column',
    border: true,
   
    initComponent: function() {

        this.percentRenderer = function(value, meta, record) {
            var result = record.get('result');
            if (result && result.failure) {
                return 'N/A';
            }
            return value + '%';
        };
        
        this.activeRenderer = function(value, meta, record) {
            if (record.get('active')) {
                meta.css = 'dashboard-row-active';
            } else {
                var result = record.get('result');
                
                if (result.modified) {
                    meta.css = 'dashboard-row-modified';
                } else if (result.warning) {
                    meta.css = 'dashboard-row-warning';
                } else if (result.failure) {
                    meta.css = 'dashboard-row-failure';
                } else {
                    meta.css = 'dashboard-row-success';
                }
                
                meta.attr += 'ext:qtitle="Result" ext:qtip="' + result.message + '"';
               
            }
        };
        
        this.reader = new Ext.ux.ObjectReader({}, [
                {name: 'id'},
                {name: 'contentSetName'},
                {name: 'contentSetId'},
                {name: 'active'},
                {name: 'host'},
                {name: 'date'},
                {name: 'itemsCompleted'},
                {name: 'totalItems'},
                {name: 'completedBytes'},
                {name: 'totalBytes'},
                {name: 'percentSuccessful'},
                {name: 'remainingTime'},
                {name: 'elapsedTime'},
                {name: 'result'},
                {name: 'lastUpdateInterval'}
            ]
        );
        
        var sharedGroupingStoreConfig = {
            reader: this.reader,
            sortInfo:{field: 'active', direction: "DESC"}
        };
        
        this.supplierStore = new Ext.data.GroupingStore(sharedGroupingStoreConfig);        
        this.consumerStore = new Ext.data.GroupingStore(sharedGroupingStoreConfig);        
        this.recentSupplierStore = new Ext.data.GroupingStore(sharedGroupingStoreConfig);        
        this.recentConsumerStore = new Ext.data.GroupingStore(sharedGroupingStoreConfig);
        
        this.supplierGrid = this.getGrid(this.supplierStore, true, true, 'Active Suppliers');
        this.consumerGrid = this.getGrid(this.consumerStore, false, true, 'Active Consumers');
        this.recentSupplierGrid = this.getGrid(this.recentSupplierStore, true, false, 'Recent Suppliers');
        this.recentConsumerGrid = this.getGrid(this.recentConsumerStore, false, false, 'Recent Consumers');
        
        var unitIntervalStore = new Ext.data.Store({reader: new Ext.ux.MapReader()});
      
        var refreshIntervalField = new Ext.form.NumberField({
            allowDecimals: false,
            allowNegative: false,
            allowBlank: false,
            width: 50,
            enableKeyEvents: true,
            minValue: 1,
            tooltip: {text: 'Enter a number 1 or greater to change how frequently to refresh the view of active and completed synchronizations on the dashboard', title: 'Dashboard Preferences - Refresh Interval'},
            style: 'margin: 3px;',
            name: 'refresh'
        });
        var refreshUnitCombo = new Ext.form.ComboBox({
            xtype: 'combo',
            store: unitIntervalStore,
            displayField: 'value',
            valueField: 'key',
            mode: 'local',
            editable: false,
            width: 55,
            triggerAction: 'all',
            tooltip: {text: 'Select a unit of time to change how long recently completed synchronizations are displayed in the dashboard', title: 'Dashboard Preferences - Refresh Interval Unit'}
        });

        var ageOutIntervalField = new Ext.form.NumberField({
            allowDecimals: false,
            allowNegative: false,
            allowBlank: false,
            width: 50,
            enableKeyEvents: true,
            minValue: 1,
            tooltip: {text: 'Enter a number 1 or greater to change how long recently completed synchronizations are displayed in the dashboard', title: 'Dashboard Preferences - Age Out Delay'},
            style: 'margin: 3px;',
            name: 'ageOut'
        });
        var ageOutIntervalUnitCombo = new Ext.form.ComboBox({
            xtype: 'combo',
            store: unitIntervalStore,
            displayField: 'value',
            valueField: 'key',
            mode: 'local',
            editable: false,
            width: 55,
            triggerAction: 'all',
            tooltip: {text: 'Select a unit of time to change how long recently completed synchronizations are displayed in the dashboard', title: 'Dashboard Preferences - Age Out Delay Unit'}
        });
        
        unitIntervalStore.loadData(EFD.util.constants.getTimeIntervals());
        
        var recentStoreConfig = { 
                reader: new Ext.ux.ObjectReader({}, [
                    {name: 'syncId'},
                    {name: 'date'}
                ]),
                sortInfo: {field: "date", direction: "DESC"}
        };
        //Collections to hold records that were active but have been expired
        this.recentSupplierMeta = new Ext.data.Store(recentStoreConfig);
        this.recentConsumerMeta = new Ext.data.Store(recentStoreConfig);

        Ext.apply(this, {
            frame: true,
            items: [
                {
                    xtype: 'toolbar',
                    columnWidth: '1',
                    border: true,
                    items: [{
                        xtype: 'tbtext',
                        text: 'Refresh every:'
                    },
                    refreshIntervalField,
                    {
                        //Wrap the combo in a panel so that it doesn't overlap the refresh button
                        //from http://extjs.com/forum/showthread.php?t=45674

                        //Other attempts that did not work:                        
                        //1. Using layoutConfig:{deferredRender:true} from http://extjs.com/forum/showthread.php?p=296512
                        //2. Calling doLayout from http://extjs.com/forum/showthread.php?t=52544
                        xtype: 'panel',
                        width: 75,
                        bodyStyle: 'background-color: transparent;',
                        items: [refreshUnitCombo]
                    },{
                        xtype: 'tbbutton',
                        iconCls: "x-tbar-loading",
                        tooltip: 'Refresh the dashboard now',
                        listeners: {
                            click: function(){
                                 this.clearTimeout(); 
                                 this.refreshDashboard();
                             }.createDelegate(this),
                             buffer: 300
                        }
                    }]       
                },{
                    xtype: 'panel',
                    layout: 'column',
                    border: true,
                    columnWidth: '1',
                    items: [{
                        cls: 'dashboard-margin',
                        columnWidth: '.54',
                        items:[this.supplierGrid]
                    },{
                        cls: 'dashboard-margin',
                        columnWidth: '.46',
                        items:[this.consumerGrid]
                    }]
                }, {
                    xtype: 'toolbar',
                    columnWidth: '1',
                    border: true,
                    items:[{
                        //In IE7 and IE8, the "Exclude empty" button is too big.
                        //It's fine in IE6, FF2/3, and Chrome
                        //From the following forum post:
                        //http://extjs.com/forum/showthread.php?t=57658
                        //it appears that there are known issues and workarounds, however using 
                        //new instead of xtype (on tb or button) did not work in this case.
                        //Using deferredRender to give the layout manager a chance to correctly calculate
                        //the button width also failed to fix the issue.
                        xtype: 'tbbutton',
                        text: 'Exclude empty',
                        enableToggle: true,
                        style: 'margin-right: 10px;',
                        toggleHandler: function(){
                            [this.recentSupplierFilters, this.recentConsumerFilters].each(function(filters){
                                var activeFilter = filters.getFilter('active');
                                var value = activeFilter.getValue();
                                var button = Ext.getCmp('empty-chooser-recent-grids');
                                if (button.pressed) {
                                    //To exclude empty syncs, enable the filter and set the value to
                                    //every option other than 'empty'
                                    var newValue = [];
                                    activeFilter.options.each(function(option) {
                                        if (option[0] !== 'empty') {
                                            newValue.push(option[0]);
                                        }
                                    });
                                    activeFilter.setValue(newValue);                                    
                                    activeFilter.setActive(true, false);
                                } else if (activeFilter.active && value.indexOf('empty') === -1){
                                    //If the filter is active and its value does not include 'empty', then
                                    //empty syncs are are not being shown.  If the filter is inactive
                                    //or empty is selected, empty syncs are shown and there is nothing to do
                                    value.push('empty');
                                    activeFilter.setValue(value);
                                }
                            });
                        }.createDelegate(this),
                        pressed: false,
                        id: 'empty-chooser-recent-grids'
                    },{
                        xtype: 'tbtext',
                        text: ' Remove entries older than:'
                    },
                    ageOutIntervalField,
                    {
                        //Wrap the combo in a panel so that it doesn't overlap the refresh button
                        // (see refreshUnitCombo above)
                        xtype: 'panel',
                        width: 75,
                        bodyStyle: 'background-color: transparent;',
                        items: [ageOutIntervalUnitCombo]
                    },{
                        xtype: 'tbbutton',
                        iconCls: "x-tbar-loading",
                        tooltip: 'Remove old entries now',
                        listeners: {
                            click: function(){                               
                                this.applyAgeOutFilterToRecentGrids(true, false);
                            }.createDelegate(this),
                            buffer: 300
                        }
                    }]
                },{
                    xtype: 'panel',
                    layout: 'column',
                    border: true,
                    columnWidth: '1',
                    items: [{
                        cls: 'dashboard-margin',
                        columnWidth: '.54',
                        items:[this.recentSupplierGrid]
                    },{
                        cls: 'dashboard-margin',
                        columnWidth: '.46',
                        items:[this.recentConsumerGrid]
                    }]
                }                
            ]
        });
                    
        EFD.dashboard.Dashboard.superclass.initComponent.apply(this, arguments);
                
        
        //Setup listeners for initialization and events
        this.on('beforeshow', function() {
            //Async call to load preferences into preference toolbar
            EFD.preferences.PreferenceManager.getPreferences({
                callback: function(preferences) {
                    this.dashboardPreferences = preferences;
                    refreshIntervalField.setValue(preferences.refresh.interval);
                    refreshUnitCombo.setValue(preferences.refresh.unit);
                    ageOutIntervalField.setValue(preferences.ageOut.interval);
                    ageOutIntervalUnitCombo.setValue(preferences.ageOut.unit);
                }
            });
        }.createDelegate(this));

        //All of the dashboard preference form elements should behave the same on their respective update events
        var intervalChangeEventConfig = {
            fn: function(){
                setTimeout(function() {
                    if(refreshIntervalField.isValid() && ageOutIntervalField.isValid()) {
                        EFD.preferences.PreferenceManager.setPreferences({
                            refresh: {
                                interval: refreshIntervalField.getValue(),
                                unit: refreshUnitCombo.getValue()
                            }
                            ,ageOut: {
                                interval: ageOutIntervalField.getValue(),
                                unit: ageOutIntervalUnitCombo.getValue()
                            }
                        });
                    }
                }, 0);
            }.createDelegate(this),
            buffer: 300
        };
        
        [refreshUnitCombo,ageOutIntervalUnitCombo].each(function(combo){
            combo.on({ 'select': intervalChangeEventConfig });
        });

        [ageOutIntervalField,refreshIntervalField].each(function(field) {
            field.on({
                'keyup': intervalChangeEventConfig,
                'blur': function() {
                    //If users enters invalid value and exits the field
                    //reset the field
                    if(!field.isValid()) {
                        EFD.preferences.PreferenceManager.getPreferences({
                            callback: function(preferences) {
                                field.setValue(preferences[field.name].interval);
                            }
                        });
                    }
                }.createDelegate(this)
            });
        });
        
        //Callback from PreferenceManager after setPreferences is called 
        //and the interval or unit has changed
    	var eventName = EFD.preferences.Properties.dashboardPreferencesUpdateEventName;

        EFD.preferences.PreferenceManager.on(eventName, function(config) {
            this.dashboardPreferences = config;
        }.createDelegate(this));
        
        EFD.preferences.PreferenceManager.getPreferences({
            callback: function(preferences) {
                this.dashboardPreferences = preferences;
                this.refreshDashboard();
            }.createDelegate(this)
        });
    }
    ,applyAgeOutFilterToRecentGrids: function(suspendGridEvents, applySort) {        
        var applyFunction = function(){                                
            var ageOutFilter = this.getAgeOutFilter();
            //Filter and sort both recent grids manually, because add doesn't handle filtering or invoke applySort()   
            [{store: this.recentSupplierStore, filters: this.recentSupplierFilters},{store: this.recentConsumerStore, filters: this.recentConsumerFilters}].each(function(storeInfo){
                var store = storeInfo.store, filters = storeInfo.filters;
                if (applySort) {               
                    var sortState = store.getSortState();
                    store.sort(sortState.field, sortState.direction);
                }
                if (store.isFiltered()) {
                    store.clearFilter(true);
                }
                store.filterBy(function(record, id){
                    return ageOutFilter(record,id) && filters.getRecordFilter()(record,id);
                });
            });                    
        }.createDelegate(this);
        
        if (suspendGridEvents) {
            //Suspend events on the recent grids, reload them, and resume events
            this.whileSuspended([this.recentSupplierGrid, this.recentSupplierGrid], applyFunction);
            [this.recentSupplierStore, this.recentConsumerStore].each(function(store){
                store.fireEvent('datachanged', store);
            });
        } else {
            applyFunction();
        }
    }
    ,clearTimeout: function() {
        window.clearTimeout(this.refreshTimeout);
    }
    
    ,refreshDashboard: function() {
        var eh = function(msg, ex) {
            this.clearTimeout();
            EFD.error.errorHandler(msg, ex);
        }.createDelegate(this);
        
        var data = {
            callback: function(response) {
                EFD.preferences.PreferenceManager.getPreferences({
                    callback: function(preferences) {
                        this.dashboardPreferences = preferences;
                        this.populateDashboard(response);
                    }.createDelegate(this)
                });
            }.createDelegate(this),
            errorHandler: eh  
        };
        
        StatusService.getCurrentSynchronizations(data);
    }
    
    ,populateDashboard: function(response) {
        //Handle the response, filtering out the already stored recent suppliers and consumers 
        //and collecting new suppliers and consumers into their own arrays
        var now = new Date().getTime(), syncMap = this.handleSyncResponse(response,now);
        
        //Suspend events on the active grids, reload them, and resume events
        this.whileSuspended([this.supplierGrid, this.consumerGrid], function(){

            this.supplierStore.loadData(syncMap.suppliers);
            this.consumerStore.loadData(syncMap.consumers);

        }.createDelegate(this));
        
        [this.supplierStore,this.consumerStore].each(function(store){
            store.fireEvent('datachanged', store);
        });

        //Suspend events on the recent grids, reload them, and resume events
        this.whileSuspended([this.recentSupplierGrid, this.recentSupplierGrid], function(){

            //Load the information from the server as usual
            this.recentSupplierStore.add(syncMap.recentSuppliers);
            this.recentConsumerStore.add(syncMap.recentConsumers);
            
            this.applyAgeOutFilterToRecentGrids(false, true);

        }.createDelegate(this));
        
        //Remove all records that are allowed that allowed by preference
        this.pruneRecentGrids();
        
        //Initialize the dashboard refresh call with an async call to load preferences
        EFD.preferences.PreferenceManager.getPreferences({
            callback: function(preferences) {
                this.dashboardPreferences = preferences;
                this.refreshTimeout = this.refreshDashboard.defer(preferences.refresh.intervalMilliseconds, this);
            }.createDelegate(this)
        });
    }
    
    ,getAgeOutFilter: function() {
        return function(record, id) {
            var now = new Date().getTime(), maxDuration = this.dashboardPreferences.ageOut.intervalMilliseconds;
            return record.get('lastUpdateInterval') + (now - record.get('date')) <= maxDuration;
        }.createDelegate(this);
    }    
    ,rowClick: function(grid, rowIndex, event, isSupplier) {
        var contentSet = {
            id: grid.getStore().getAt(rowIndex).get('contentSetId'),
            name: grid.getStore().getAt(rowIndex).get('contentSetName'),
            supplier : isSupplier
        };
        
        var callback = function() {
            if (grid.getStore().getAt(rowIndex).get('active')) {
                Ext.getCmp('userPanel').showStatus(contentSet);
            } else {
                Ext.getCmp('userPanel').showHistory(contentSet);    
            }
            this.ownerCt.un('afterlayout', callback, this);
        };
        
        this.ownerCt.on('afterlayout', callback, this);
        this.ownerCt.showNavigator();
    }
    
    ,getGrid: function(store, isSupplier, isActive, title) {

        var emptyGridText = 'No ' + title;

        var columns = [
            {header: "State", sortable: true, width: 50, dataIndex: 'active', renderer: this.activeRenderer},
            {header: "Name",  sortable: true, dataIndex: 'contentSetName'}
        ];

        var filtersConfig = [
            {type: 'string',  dataIndex: 'contentSetName', icon: '/images/find.png'}
        ];
        
        if (isSupplier) {
            columns.push({header: "Host", sortable: true, dataIndex: 'host'});
            filtersConfig.push({type: 'string',  dataIndex: 'host', icon: '/images/find.png'});
        }
        
        if (isActive) {
            columns.push(
                {header: "Elapsed", sortable: true, dataIndex: 'elapsedTime', renderer: EFD.util.renderers.TIME_INTERVAL},
                {header: "Est. Remaining", sortable: true, dataIndex: 'remainingTime', renderer: EFD.util.renderers.TIME_INTERVAL},
                {header: "Last Activity", sortable: true, dataIndex: 'lastUpdateInterval', renderer: function(value, meta, record) {
                    if (value === 0) {
                        return 'N/A';
                    }
                    return EFD.util.formatTime(value)+' ago';
                }},
                {header: "%", sortable: true, width: 50, dataIndex: 'percentSuccessful', renderer: this.percentRenderer}
            );

            filtersConfig.push({
                type: 'lastActivity',  
                dataIndex: 'lastUpdateInterval', 
                options: [{id: 5, text: '> 5m'}, {id: 10, text:'> 10m'}, {id: 15, text: '> 15m'}, {id: 30, text:'> 30m'}, {id:60, text:'> 60m'}]
            });
        } else {
            columns.push(                
                //Use the date dataIndex instead of lastUpdateInterval, because that is 0 for empty syncs
                {header: "Last Activity", sortable: true, dataIndex: 'date', renderer: function(value, meta, record) {
                    record.modified.date = undefined;
                    
                    //For recent sync events, lastUpdateInterval is only set on the record once, when it first comes back
                    //as a getSynchronizationData response element, so calculate the offset based on the browser time
                    //when it was stored.
                    return EFD.util.formatTime( record.get('lastUpdateInterval') + (new Date().getTime() - value))+' ago';
                }},
                {header: "%", sortable: true, width: 50, dataIndex: 'percentSuccessful', renderer: this.percentRenderer}
            );
            filtersConfig.push({
                type: 'active',  
                dataIndex: 'active', 
                options: ['modified', 'warning', 'failure', 'empty']
            });
        }

        
        var filters = new Ext.grid.GridFilters({
            filters: filtersConfig,
            local: true
        });
        
        var grid = new Ext.grid.GridPanel({
            autoScroll : true,
            frame: true,
            store: store,
            columns: columns,
            plugins: filters,
            view: new Ext.grid.GroupingView({
                // custom grouping text template to display the number of items per group
                groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})', 
                emptyText: emptyGridText
            }),
            viewConfig: { 
                autoFill: true
            },
            stripeRows: true,
            border: false,
            title: title,
            autoHeight: true,
            listeners: {
                rowclick: this.rowClick.createDelegate(this,[isSupplier], true)
            },
            style: 'margin-bottom: 15px;'
        });
            
        grid.getView().getRowClass = function(record, index){
            return 'cursor-icon-hand ';
        };
        
        //Maintain references to filters so that filters.getRecordFilter()
        //can be called after adding new data to the store
        if (isActive) {
            if (isSupplier) {
                this.activeSupplierFilters = filters;
            } else {
                this.activeConsumerFilters = filters;
            }
        } else {
            if (isSupplier) {
                this.recentSupplierFilters = filters;
            } else {
                this.recentConsumerFilters = filters;
            }
        }
        return grid;
    }
    
    ,whileSuspended: function(observables, fn) {
        this.suspendEvents(observables);
        fn();
        this.resumeEvents(observables);
    }
    
    ,suspendEvents: function(observables) {
        observables.each(function(o) {
            o.suspendEvents();
        });
    }
    
    ,resumeEvents: function(observables) {
        observables.each(function(o) {
            o.resumeEvents();
        });
    }

    ,createFindRecordByFieldFunction: function(field,fieldValue){
        return function(record,id) {
             return record.get(field) == fieldValue;
        };
    }
    
    ,handleSyncResponse: function(response,now) {
        var syncMap = { consumers: [], suppliers: [], recentConsumers: [], recentSuppliers: [] }, newRecord, recentIndex, recentRecord;
        var RecentEntryRecord = Ext.data.Record.create([{name: 'date'},{name: 'syncId'}]); 

        //Iterate over all the sync information, ignoring any inactive syncs that
        //are already accounted for or that are already too old
        for (var i=0; i < response.length; i = i + 1) {
            if (response[i].supplier) {
                if (response[i].active) {
                    syncMap.suppliers.push(response[i]);
                } else  {
                    recentIndex = this.recentSupplierMeta.findBy(this.createFindRecordByFieldFunction('syncId',response[i].id));
                    if (recentIndex === -1) {
                        newRecord = this.reader.read(response[i]).records[0];
                        //Override the date field here.  The server time is irrelevant, because everything is displayed
                        //in terms of browser time
                        newRecord.set('date', now);
                        
                        syncMap.recentSuppliers.push(newRecord);
                        this.recentSupplierMeta.addSorted(new RecentEntryRecord({
                            'date': now,
                            'syncId': response[i].id
                        }));
                    } else {
                        recentRecord = this.recentSupplierMeta.getAt(recentIndex);
                        recentRecord.set('date', now);
                    }
                }
            } else {
                if (response[i].active) {
                    syncMap.consumers.push(response[i]);
                } else {
                    recentIndex = this.recentConsumerMeta.findBy(this.createFindRecordByFieldFunction('syncId',response[i].id));
                    if (recentIndex === -1) {
                        newRecord = this.reader.read(response[i]).records[0];
                        //Override the date field here.  The server time is irrelevant, because everything is displayed
                        //in terms of browser time
                        newRecord.set('date', now);

                        syncMap.recentConsumers.push(newRecord);
                        this.recentConsumerMeta.addSorted(new RecentEntryRecord({
                            'date': now,
                            'syncId': response[i].id
                        }));                    
                    } else {
                        recentRecord = this.recentConsumerMeta.getAt(recentIndex);
                        recentRecord.set('date', now);
                    }
                }
            }
        }
        
        return syncMap;
    }
    
    ,pruneRecentGrids: function() {
        var maxEntries = EFD.preferences.Properties.maximumRecentEntries;
        var storeInfo = [{
            store : this.recentSupplierStore,
            meta : this.recentSupplierMeta,
            filters: this.recentSupplierFilters
        },{
            store : this.recentConsumerStore,
            meta: this.recentConsumerMeta,
            filters: this.recentConsumerFilters
        }];

        var findByFieldValue = this.createFindRecordByFieldFunction;
        storeInfo.each(function(storeInfo){
            var store = storeInfo.store, meta = storeInfo.meta, filters = storeInfo.filters;
            if (store.getCount() > maxEntries) {
                this.whileSuspended([store, meta], function() {
                    var recordsToDelete = meta.getRange(maxEntries, meta.getCount()-1);
                    recordsToDelete.each(function(recordMeta){
                        var syncId = recordMeta.get('syncId');
                        var matches = store.queryBy(findByFieldValue('id',syncId), this);
                        if (matches.getCount() == 1) {
                            store.remove(matches.get(0));
                        }
                    });
                    recordsToDelete.each(function(recordMeta) {
                        meta.remove(recordMeta);
                    });
                }); 
            }
            
            this.applyAgeOutFilterToRecentGrids(false,true);
            
            store.fireEvent('datachanged', store);
            meta.fireEvent('datachanged', meta);
        }.createDelegate(this));
    }
});

Ext.reg('dashboard', EFD.dashboard.Dashboard);
