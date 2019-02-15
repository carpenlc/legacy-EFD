/*global Ext, EFD, HistoryHelper */

Ext.ns('EFD.history');

EFD.history.BaseSyncPanel = Ext.extend(Ext.Panel, {
    
    autoScroll: true,
    autoHeight: true,
    
    /**
     * These must be implemented
     */
    getRecord: Ext.emptyFn,
    getColumns: Ext.emptyFn,
    
    initComponent: function() {

        var reader = new Ext.ux.grid.livegrid.JsonReader({
            root: 'page',
            totalProperty: 'count'
          },
          this.getRecord()
        );
        
        this.statusRenderer = function(value, meta, record) {
            if (record.get('failures')) {
                meta.css = 'history-row-failed';
                meta.attr += 'ext:qtitle="Warning" ext:qtip="Some items failed during this synchronization"';
            } 
        };
        
        this.filters = new Ext.grid.GridFilters({
            filters: [
                {type: 'date',  dataIndex: 'timestamp'}
            ]
        });
        
        this.store = new Ext.ux.grid.livegrid.Store({
            autoLoad: true,
            bufferSize: 100,
            reader: reader,
            proxy: new Ext.ux.DWRProxy({
                dwrFunction: HistoryHelper.getSynchronizations,
                dwrFunctionArgs: this.getParams.createDelegate(this)
            })
        });
        
        var columns = this.getColumns();
        columns.unshift({header: "", sortable: false, hideable: false, width: 20, renderer: this.statusRenderer});
        
        var view = new Ext.ux.grid.livegrid.GridView({
            autoFill: true,
            nearLimit: 25,
            loadMask : {
                msg : 'Please wait...'
            }
        });
        
        var grid = new Ext.ux.grid.livegrid.GridPanel({
            style: 'padding: 4px',
            store: this.store,
            columns: columns,
            stripeRows: true,
            loadMask: true,
            plugins: this.filters,
            frame: true,
            title: 'Synchronizations',
            selModel: new Ext.ux.grid.livegrid.RowSelectionModel(),
            view: view,
            bbar: new Ext.ux.grid.livegrid.Toolbar({
                view: view,
                displayInfo: true
            }),
            tbar: [{
                text: 'Include empty synchronizations',
                enableToggle: true,
                toggleHandler: this.reload.createDelegate(this),
                pressed: false,
                id: 'empty-chooser-'+this.id
            }],
            listeners: {
                rowclick: this.rowClick.createDelegate(this)
            }
        });
        
       grid.trackMouseOver = true;
       grid.getView().getRowClass = function(record, index){
            return 'cursor-icon-hand ';
        };
        
        Ext.apply(this, {
            items: [grid]
        });
        
        EFD.history.BaseSyncPanel.superclass.initComponent.apply(this, arguments);
        
        this.on('aftersave', this.changeTitle, this);
        
        var resizeHistoryGridCallback = function(){
            var minHeight = (grid.view.rowHeight || 26 ) + grid.view.hdHeight + (grid.tbar ? grid.tbar.getHeight() : 26);    
            var containerHeight = this.container.getHeight() || 300;
            var newHeight = Math.max(minHeight, containerHeight);
            if (!grid.rendered || newHeight !== grid.el.getHeight()) {
                grid.setHeight(newHeight);
            }
        }.createDelegate(this);
        
        this.on('resize', resizeHistoryGridCallback);
        this.store.on('load', resizeHistoryGridCallback);

    } // eo function initComponent
    
    ,changeTitle: function(panel, contentSet) {
        if (typeof contentSet == 'object') {
            if (this.contentset.id == contentSet.id) {
                this.setTitle(contentSet.name+' - History');
            }
        }
    }
    
    ,reload:function() {
        this.store.load({
            params: {
                start: 0, 
                limit: EFD.util.constants.getPageSize(),
                dwrFunctionArgs: this.getParams.createDelegate(this)
            }
        });
    }
    
    ,getParams: function(params) {
        var startDate = null;
        var endDate = null;
        var f = Ext.getCmp('empty-chooser-'+this.id);
        var showEmptyRecords = f ? f.pressed : false;
        var start = 0;
        
        var filters = this.filters.getFilterData();
        filters.each(function (filter) {
            if (filter.field === 'timestamp') {
                if (filter.data.comparison === 'lt') {
                    endDate = Date.parseDate(filter.data.value+' 23:59', 'm/d/Y H:i');
                }
                if (filter.data.comparison === 'gt') {
                    startDate = Date.parseDate(filter.data.value+' 00:00', 'm/d/Y H:i');
                }
                if (filter.data.comparison === 'eq') {
                    startDate = Date.parseDate(filter.data.value+' 00:00', 'm/d/Y H:i');
                    endDate = Date.parseDate(filter.data.value+' 23:59', 'm/d/Y H:i');
                }
            }
        });
        
        if (params && params.start !== undefined) {
            start = params.start;
        }
        var limit = EFD.util.constants.getPageSize();
        if (params && params.limit !== undefined) {
            limit = params.limit;
        }
        
        return [this.contentset.id, startDate, endDate, showEmptyRecords, start, limit];
    }
    
    ,rowClick: function(grid, rowIndex, event) {
        var syncId = grid.getStore().getAt(rowIndex).get('id');
        
        var xtype = 'consumerdetailspanel';
        if (this.contentset.supplier) {
            xtype = 'supplierdetailspanel';
        }
        
        var win = new Ext.Window({
            
            modal: true,    
            width: 870,
            autoHeight: true,
            plain: true,
            title: 'Details',
            autoScroll: true,
            
            items: [{
                contentSetId: this.contentset.id,
                syncId: syncId,
                xtype: xtype
            }]
        });
        win.show(this);
    }    
});









EFD.history.SupplierSyncPanel = Ext.extend(EFD.history.BaseSyncPanel, {
    
    getRecord: function() {
        return [
            {name: 'id'},
            {name: 'timestamp'},
            {name: 'host'},
            {name: 'elapsedTime'},
            {name: 'adds'},
            {name: 'bytesAdded'},
            {name: 'failures'}
        ];
    }
    
    ,getColumns: function() {
        return [
            {header: "Date", sortable: false, dataIndex: 'timestamp', renderer: EFD.util.renderers.DATE},
            {header: "Duration", sortable: false, dataIndex: 'elapsedTime', renderer: EFD.util.renderers.TIME_INTERVAL},
            {header: "Host", sortable: false, dataIndex: 'host'},
            {header: "Items Supplied", sortable: false, dataIndex: 'adds'},
            {header: "Items Failed", sortable: false, dataIndex: 'failures'},
            {header: "Bytes Supplied", sortable: false, dataIndex: 'bytesAdded', renderer: EFD.util.renderers.FILE_SIZE}
        ];
    }
    
});













EFD.history.ConsumerSyncPanel = Ext.extend(EFD.history.BaseSyncPanel, {
    
    getRecord: function() {
        return [
            {name: 'id'},
            {name: 'timestamp'},
            {name: 'elapsedTime'},
            {name: 'adds'},
            {name: 'updates'},
            {name: 'deletes'},
            {name: 'failures'},
            {name: 'bytesAdded'},
            {name: 'bytesUpdated'},
            {name: 'bytesDeleted'},
            {name: 'bytesFailed'}
        ];
    }
    
    ,getColumns: function() {
        return [
            {header: "Date", sortable: false, width: 125, dataIndex: 'timestamp', renderer: EFD.util.renderers.DATE},
            {header: "Duration", sortable: false, dataIndex: 'elapsedTime', renderer: EFD.util.renderers.TIME_INTERVAL},
            {header: "Items Added", sortable: false, dataIndex: 'adds'},
            {header: "Items Updated", sortable: false, dataIndex: 'updates'},
            {header: "Items Deleted", sortable: false, dataIndex: 'deletes'},
            {header: "Items Failed", sortable: false, dataIndex: 'failures'},
            {header: "Bytes Added", sortable: false, dataIndex: 'bytesAdded', renderer: EFD.util.renderers.FILE_SIZE},
            {header: "Bytes Updated", sortable: false, dataIndex: 'bytesUpdated', renderer: EFD.util.renderers.FILE_SIZE},
            {header: "Bytes Deleted", sortable: false, dataIndex: 'bytesDeleted', renderer: EFD.util.renderers.FILE_SIZE},
            {header: "Bytes Failed", sortable: false, dataIndex: 'bytesFailed', renderer: EFD.util.renderers.FILE_SIZE}
        ];
    }
    
});









EFD.history.BaseDetailsPanel = Ext.extend(Ext.Panel, {
    
    /**
     * These must be implemented
     */
    getRecord: Ext.emptyFn,
    getColumns: Ext.emptyFn,
    getParams: Ext.emptyFn,
    getFilters: Ext.emptyFn,
    
    initComponent: function() {
    
        var reader = new Ext.data.JsonReader({
                root: 'page',
                totalProperty: 'count'
            }, 
            this.getRecord()
        );
        
        this.store = new Ext.data.Store({
            reader: reader,
            proxy: new Ext.ux.DWRProxy({
                dwrFunction: HistoryHelper.getSynchronizationDetails,
                dwrFunctionArgs: this.getParams.createDelegate(this)
            })
        });
        
        this.statusRenderer = function(value, meta, record) {
            if (record.get('failed')) {
                meta.css = 'history-row-failed';
            } else {
                meta.css = 'history-row';
            }
        };
        
        this.columnsAreSortable = true;
        
        var columns = this.getColumns.createDelegate(this)();
        columns.unshift({header: "", sortable: false, hideable: false, width: 20, renderer: this.statusRenderer});
        
        this.filters = new Ext.grid.GridFilters({
            filters: this.getFilters()
        });
        
        var grid = new Ext.grid.GridPanel({
            store: this.store,
            columns: columns,
            stripeRows: true,
            viewConfig: {
                autoFill: true
            },
            plugins: this.filters,
            bbar: new Ext.ux.PagingToolBar({
                pageSize: EFD.util.constants.getPageSize(),
                store: this.store,
                displayInfo: true,
                displayMsg: 'Displaying synchronization details {0} - {1} of {2}',
                emptyMsg: 'No synchronization details to display',
                dwrParamFunction: this.getParams.createDelegate(this)
            }),
            style: 'padding: 4px',
            height: 400,
            width: 850,
            frame: true,
            title: 'Actions',
            loadMask: true
        });
        
        Ext.apply(this, {
            items: [grid]
        });
        
        grid.trackMouseOver = true;
      
        EFD.history.BaseDetailsPanel.superclass.initComponent.apply(this, arguments);
        
        this.on('afterlayout', function() {        
            this.store.load({
                params: {
                    start: 0, 
                    limit: EFD.util.constants.getPageSize(),
                    dwrFunctionArgs: [this.contentSetId, this.syncId, null, null]
                }
            });        
        }, this);
        
    } // eo function initComponent
    
    ,submit:function() {
        this.store.load({
            params: {
                start: 0, 
                limit: EFD.util.constants.getPageSize(),
                dwrFunctionArgs: this.createDelegate(this)
            }
        });
    }
});



EFD.history.ConsumerDetailsPanel = Ext.extend(EFD.history.BaseDetailsPanel, {
    
    getRecord: function() {
        return [
            {name: 'timestamp'},
            {name: 'action'},
            {name: 'path'},
            {name: 'size'},
            {name: 'successful'},
            {name: 'failed'},
            {name: 'status'}
        ];
    }
    
    ,getFilters: function() {
        return [
            {
                type: 'list',  
                dataIndex: 'action', 
                options: EFD.util.constants.getContentEventActions(),
                listeners: {
                    // This essentially adds 'radio' functionality to the ListFilter in that it only allows
                    // one item to be selected at a time
                    'update': function(list, checked) {
                        list.menu.items.each(function(item) {
                            if (item.itemId !== checked.itemId) {
                                item.setChecked(false, true);
                            }
                        });
                        if (checked.checked) {
                            list.value = [checked.itemId];
                        }
                    }
                }
            },
            {type: 'string',  dataIndex: 'path'}
        ];
    }
    
    ,getColumns: function() {
        var sortable = true;
        return [
            {header: "Date", sortable: sortable, width: 125, dataIndex: 'timestamp', renderer: EFD.util.renderers.DATE},
            {header: "Action", sortable: sortable, dataIndex: 'action'},
            {header: "Path", sortable: sortable, width: 250, dataIndex: 'path'},
            {header: "Size", sortable: sortable, dataIndex: 'size', renderer: EFD.util.renderers.FILE_SIZE},
            {header: "Successful", sortable: sortable, dataIndex: 'successful', renderer: EFD.util.renderers.FILE_SIZE},
            {header: "Failed", sortable: sortable, dataIndex: 'failed', renderer: EFD.util.renderers.FILE_SIZE},
            {header: "Result", sortable: sortable, dataIndex: 'status'}
        ];
    }
    
    ,getParams: function(params) {
        var action = null;
        var path = null;
        
        var filters = this.filters.getFilterData();
        filters.each(function (filter) {
            if (filter.field === 'action') {
                action = filter.data.value[0];
            }
            if (filter.field === 'path') {
                path = filter.data.value;
            }
        });
        
        var start = 0;
        if (params && params.start !== undefined) {
            start = params.start;
        }
        
        var limit = EFD.util.constants.getPageSize();
        if (params && params.limit !== undefined) {
            limit = params.limit;
        }
        
        return [this.contentSetId, this.syncId, action, path, start, limit];
    }
    
});





EFD.history.SupplierDetailsPanel = Ext.extend(EFD.history.BaseDetailsPanel, {
    
    getRecord: function() {
        return [
            {name: 'timestamp'},
            {name: 'path'},
            {name: 'size'},
            {name: 'successful'},
            {name: 'failed'},
            {name: 'status'}
        ];
    }
    
    ,getFilters: function() {
        return [{type: 'string',  dataIndex: 'path'}];
    }
    
    ,getColumns: function() {
        var sortable = true;
        return [
            {header: "Date", sortable: sortable, width: 125, dataIndex: 'timestamp', renderer: EFD.util.renderers.DATE},
            {header: "Path", sortable: sortable, width: 250, dataIndex: 'path'},
            {header: "Size", sortable: sortable, dataIndex: 'size', renderer: EFD.util.renderers.FILE_SIZE},
            {header: "Successful", sortable: sortable, dataIndex: 'successful', renderer: EFD.util.renderers.FILE_SIZE},
            {header: "Failed", sortable: sortable, dataIndex: 'failed', renderer: EFD.util.renderers.FILE_SIZE},
            {header: "Result", sortable: sortable, dataIndex: 'status'}
        ];
    }
    
    ,getParams: function(params) {
        var path = "";
        
        var filters = this.filters.getFilterData();
        filters.each(function (filter) {
            if (filter.field === 'path') {
                path = filter.data.value;
            }
        });
        
        var start = 0;
        if (params && params.start !== undefined) {
            start = params.start;
        }
        
        var limit = EFD.util.constants.getPageSize();
        if (params && params.limit !== undefined) {
            limit = params.limit;
        }
        
        return [this.contentSetId, this.syncId, null, path, start, limit];
    }
});




Ext.reg('suppliersyncpanel', EFD.history.SupplierSyncPanel);
Ext.reg('consumersyncpanel', EFD.history.ConsumerSyncPanel);
Ext.reg('supplierdetailspanel', EFD.history.SupplierDetailsPanel);
Ext.reg('consumerdetailspanel', EFD.history.ConsumerDetailsPanel);
