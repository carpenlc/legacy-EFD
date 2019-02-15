/*global Ext, EFD, ContentSetHelper, AllowedHostService */

Ext.ns('EFD.cs');

EFD.cs.ContentSetForm = Ext.extend(Ext.ux.DWRForm, {
 
    // configurables
    frame: true,
    labelWidth: 80,
    autoScroll: true,
    
    initComponent: function() {
        
        Ext.apply(this, {
            labelAlign: 'right',
            labelWidth: 250,
            bodyStyle:'padding:5px',
            width: 600, 
            buttons: [{
                text: 'Save',
                handler: this.submit,
                scope: this
            }]
        });
        
        EFD.cs.ContentSetForm.superclass.initComponent.apply(this, arguments);
        
        this.on('formrendered', this.formRendered, this);
        this.on('beforesave', this.prepareContentSet, this);
        this.on('aftersave', this.afterSave, this);
        this.on('pathupdated', this.updatePath, this);
        this.on('statuschanged', this.updateStatus, this);
        this.on('nodedeleted', this.remove, this);
    } // eo function initComponent
    
    ,remove: function(panel, contentSet) {
        if (this.contentset) {
            if (this.contentset.id == contentSet.id) {
                Ext.getCmp('window-tab-panel').remove(this);
            }
        }
    }
    
    ,updatePath: function(panel, id, path) {
        if (this.contentset) {
            if (this.contentset.id == id) {
                this.contentset.displayPath = path;
            }
        }
    }
    
    ,updateStatus: function(panel, contentSet) {
        if (this.contentset) {
            if (this.contentset.id == contentSet.id) {
                this.contentset.enabled = !contentSet.enabled;
            }
        }
    }
    
    ,formRendered: function() {
        if (this.contentset) {
            this.onLoad();
        } else {
            var callback = function(response) {
                this.getForm().setValues(response);
                this.getForm().clearInvalid();
                this.loadMask.hide();
            };
            this.template.call(this, {callback: callback.createDelegate(this)});
            this.on('aftersave', this.afterNewSave, this);
        }
    }
    
    ,onLoad: function() {
        var callback = this.doFormLoad.createDelegate(this);
        ContentSetHelper.getContentSet(this.contentset.id, {callback: callback});
    }
    
    ,prepareContentSet: function(form, contentSet) {
        if (this.contentset) {
            contentSet.id = this.contentset.id;
            contentSet.enabled = this.contentset.enabled;
            contentSet.displayPath = this.contentset.displayPath;
        }
        
        if (contentSet.supportsGbsTransport === 'on') {
            contentSet.supportsGbsTransport = true;
        }
    }
    
    ,afterSave: function(id, contentSet) {
        EFD.util.msg(contentSet.name, 'Saved');
        this.setTitle(contentSet.name);
    }
    
    /**
     * After saving a new content set, we want to change the internal state so that future updates
     * change the newly created content set instead of attempting to create
     * a new one.
     */
    ,afterNewSave: function(form, contentSet, id) {
        this.contentset = {
            id: id,
            enabled: false
        };
        
        this.un('aftersave', this.afterNewSave, this);
    }
    
 
});


EFD.cs.SchedulePanel = Ext.extend(Ext.Panel, {
    initComponent: function() {
        var storeInterval = new Ext.data.Store({reader: new Ext.ux.MapReader()});
        
        var intervalCombo = new Ext.form.ComboBox({
            fieldLabel: 'Interval Unit',                            
            hiddenName: 'inventoryIntervalUnit',
            store: storeInterval,
            displayField: 'value',
            valueField: 'key',
            mode: 'local',
            editable: false,
            anchor: '50%',
            triggerAction: 'all'                                                 
        });        
        
        var runtimeCombo = new Ext.form.ComboBox({                            
            labelSeparator: '',
            fieldLabel: '',
            hiddenName: 'runtimeUnit',
            store: storeInterval,
            displayField: 'value',
            valueField: 'key',
            mode: 'local',
            editable: false,
            anchor: '50%',
            triggerAction: 'all',
            lazyInit: false                             
        });
        
        storeInterval.on('load', function() {
           runtimeCombo.setValue('M'); 
        });
        
        storeInterval.loadData(EFD.util.constants.getTimeIntervals());
        
        Ext.apply(this, {
            title:'Scheduling',
            layout: 'form',
            autoScroll: true,
            defaultType: 'textfield',
            items: [{
                xtype: 'radiogroup',
                fieldLabel: 'Type',
                items: [
                    {boxLabel: 'Interval', name: 'scheduleType', inputValue: 'I'},
                    {boxLabel: 'Daily', name: 'scheduleType', inputValue: 'D'},
                    {boxLabel: 'Cron (advanced)', name: 'scheduleType', inputValue: 'C'}
                ],
                listeners: {
                    check: function(field, checked) { 
                        if (!checked) {
                            return;
                        }
                        
                        if (field.getRawValue() === 'I') {
                            Ext.getCmp('schedule-interval-'+this.id).show();
                            Ext.getCmp('schedule-daily-'+this.id).hide();
                            Ext.getCmp('schedule-cron-'+this.id).hide();
                        } else if (field.getRawValue() === 'D') {
                            Ext.getCmp('schedule-interval-'+this.id).hide();
                            Ext.getCmp('schedule-daily-'+this.id).show();
                            Ext.getCmp('schedule-cron-'+this.id).hide();
                        } else if (field.getRawValue() === 'C') {
                            Ext.getCmp('schedule-interval-'+this.id).hide();
                            Ext.getCmp('schedule-daily-'+this.id).hide();
                            Ext.getCmp('schedule-cron-'+this.id).show();
                        }
                    }.createDelegate(this)
                }
            },{
                xtype: 'fieldset',
                title: 'Daily',
                id: 'schedule-daily-'+this.id,
                autoHeight: true,
                items: [{
                    xtype: 'lovcombo',
                    fieldLabel: 'Run on',
                    name: 'scheduleDays',
                    editable: false,
                    allowBlank: false,
                    minListWidth: 100,
                    width: 400,
                    store: [
                        [1, 'Sunday'],
                        [2, 'Monday'],
                        [3, 'Tuesday'],
                        [4, 'Wednesday'],
                        [5, 'Thursday'],
                        [6, 'Friday'],
                        [7, 'Saturday']
                    ],
                    triggerAction: 'all',
                    mode: 'local',
                    itemCls: 'required'
                },{
                    name: 'scheduleTime',
                    fieldLabel: 'Start at',  
                    xtype: 'timefield',
                    allowBlank: false,
                    format: 'H:i',
                    itemCls: 'required'
                }, {
                    xtype: 'numberfield',
                    fieldLabel: '(optionally) Stop after',
                    name: 'runtimeInterval',
                    allowDecimals: false,
                    allowNegative: false, 
                    emptyText: 'Enter an optional duration',
                    anchor: '60%',
                    width: 200                                         
                }, 
                runtimeCombo]
            },{
                xtype: 'fieldset',
                title: 'Interval',
                id: 'schedule-interval-'+this.id,
                autoHeight: true,
                items: [{
                    xtype: 'numberfield',
                    fieldLabel: this.intervalLabel,
                    name: 'inventoryInterval',
                    allowBlank: false,
                    allowDecimals: false,
                    allowNegative: false, 
                    anchor: '50%',
                    itemCls: 'required'
                }, 
                intervalCombo]
            },{
                xtype: 'fieldset',
                title: 'Advanced Cron Scheduling',
                id: 'schedule-cron-'+this.id,
                autoHeight: true,
                items: [{
                    xtype: 'helpfield',
                    contentEl: 'cron',
                    fieldLabel: 'Cron Expression',
                    name: 'cronExpression',
                    itemCls: 'required'
                }]
            }]                   
        });
        
        EFD.cs.SchedulePanel.superclass.initComponent.apply(this, arguments);
    }
    
    ,formRendered: function(form) {
        form.findField('scheduleType').setValue('I');
        form.findField('scheduleType').fireEvent('check', form.findField('scheduleType'), true);
    }  
    ,afterFormLoad: function(form, expressions) {        
        if (expressions.length > 0) {
            var s = expressions[0];
             
            if (EFD.cron.isDailyExpression(s.cronExpression)) {
                form.findField('scheduleType').setValue('D');
                var parsed = EFD.cron.parseDaily(s.cronExpression);
            
                if (s.duration > 0) {
                    form.findField('runtimeInterval').setValue(s.duration);
                }
                form.findField('runtimeUnit').setValue(s.durationUnit);
                form.findField('scheduleTime').setValue(parsed.timeValue);
                form.findField('scheduleDays').setValue(parsed.days);
            } else {
                form.findField('scheduleType').setValue('C');
                form.findField('cronExpression').setValue(s.cronExpression);
            }
            this.expressionid = s.id;
        } else {
            form.findField('scheduleType').setValue('I');
        }
        
        form.findField('scheduleType').fireEvent('check', form.findField('scheduleType'), true);
    }
    ,preSubmit: function(form, contentSet) {
        if (contentSet.scheduleType === 'D' || contentSet.scheduleType === 'C') {
            var expr = {};
            
            if (contentSet.scheduleType === 'D') {
                expr.cronExpression = EFD.cron.formatDaily(contentSet.scheduleTime, form.findField('scheduleDays').getCheckedValue());
                var duration = form.findField('runtimeInterval').getValue();
                if (duration > 0) {
                    expr.duration = duration;
                    expr.durationUnit = form.findField('runtimeUnit').getValue();
                }
            } else {
                expr.cronExpression = contentSet.cronExpression;
            }
            if (this.expressionid !== undefined) {
                expr.id = this.expressionid;
            }

            contentSet.scheduleExpressions = [expr];
            delete contentSet.inventoryInterval;
            delete contentSet.inventoryIntervalUnit;
        } else {
            contentSet.scheduleExpressions = [];
        }
        
        delete contentSet.cronExpression;
        delete contentSet.scheduleDays;
        delete contentSet.scheduleTime;
        delete contentSet.scheduleType;
        delete contentSet.runtimeInterval;
        delete contentSet.runtimeUnit;
    }
});



EFD.cs.SupplierForm = Ext.extend(EFD.cs.ContentSetForm, {
 
    initComponent: function() {
        var reader = new Ext.ux.ObjectReader({}, [
           {name: 'id'},
           {name: 'alias'},
           {name: 'commonName'}
        ]);
        
        var store = new Ext.data.Store({
            proxy: new Ext.ux.DWRProxy({
                dwrFunction: AllowedHostService.list
            }),
            reader: reader
        });
        store.load({});
        
        this.schedule = new EFD.cs.SchedulePanel({intervalLabel: 'Inventory Generation Interval'}); 
        
        Ext.apply(this, {
            saveMethod: ContentSetHelper.saveSupplier, 
            items: [{
                xtype: 'tabpanel',
                plain: true,
                activeTab: 0,
                id: 'form-tab-panel-'+this.id,
                // only fields from an active tab are submitted
                // if the following line is not persent
                deferredRender: false,
                width: 800,
                height: 235,
                defaults: {bodyStyle:'padding:10px'},
                items:[{
                    title: 'Details',
                    layout:'form',         
                    
                    defaultType: 'textfield',
                    items:[{
                        fieldLabel: 'Name',
                        name:  'name',
                        allowBlank: false,
                        itemCls: 'required',                    
                        maxLength: 80,
                        regex: new RegExp('[a-zA-Z_0-9-]*'),
                        regexText: 'Name is only allow any combination of letter, digit, hyphen, or/and underscore',
                        //validationEvent: true,
                        //validator: checkEmpty, 
                        anchor: '75%'
                    }, {
                        fieldLabel: 'Description',
                        name: 'description',
                        anchor:'95%'
                    }, {
                        fieldLabel: 'Content Set Directory',
                        name: 'path',
                        allowBlank: false,
                        itemCls: 'required',
                        anchor: '75%'            
                    }, {
                        xtype: 'lovcombo',
                        fieldLabel: 'Allowed Hosts',
                        name: 'allowedHosts',
                        width: 400,
                        hideOnSelect: false,
                        lazyInit: false,
                        editable: false,
                        store: store,
                        valueField: 'alias',
                        displayField: 'alias',
                        triggerAction:'all',
                        mode:'local'
                    }, {
                        xtype: 'checkbox',
                        fieldLabel: 'Support GBS',
                        name: 'supportsGbsTransport',
                        disabled: (EFD.util.constants.isGbsEnabled() === false),
                        listeners: {
                            check: function (checkbox, checked) {
                                this.changeSbmPanel(checked);
                            },
                            scope: this
                        }
                    }]
                }, this.schedule, {
                    title:'SBM Details',
                    layout: 'form',
                    id: 'sbm-panel-'+this.id,
                    
                    defaultType: 'textfield',
                    items: [{                   
                        fieldLabel: 'Host Name',
                        name: 'sbmName',
                        allowBlank: false,
                        itemCls: 'required',
                        width: 200
                    },{
                        xtype: 'numberfield',
                        fieldLabel: 'Port',
                        allowBlank: false,
                        allowDecimals: false,
                        allowNegative: false,
                        name: 'sbmPort',
                        width: 40,
                        itemCls: 'required'                  
                    },{                    
                        fieldLabel: 'Username',
                        name: 'sbmUser',
                        allowBlank: false,
                        itemCls: 'required'
                    },{                    
                        fieldLabel: 'Password',
                        name: 'sbmPassword',
                        allowBlank: false,
                        itemCls: 'required',
                        inputType: 'password'
                    },{                    
                        fieldLabel: 'Directory',
                        name: 'sbmDir',
                        allowBlank: false,
                        itemCls: 'required',
                        width: 200
                    },{
                        xtype: 'checkbox',
                        fieldLabel: 'Passive',
                        name: 'sbmPassive'
                    },{
                        xtype: 'checkbox',
                        fieldLabel: 'Implicit FTPS',
                        name: 'sbmImplicit'
                    }]
                }]
            }]               
        }); // eo apply
 
        // call parent
        EFD.cs.SupplierForm.superclass.initComponent.apply(this, arguments);
        
        this.on('afterload', this.afterFormLoad, this);
        this.on('beforesave', this.preSubmit, this);
 
    } // eo function initComponent
    ,formRendered: function() {
        if (!this.contentset) {
            this.schedule.formRendered(this.getForm());
            this.changeSbmPanel(false);
        }
        EFD.cs.SupplierForm.superclass.formRendered.apply(this, arguments);
    } 
    ,afterFormLoad: function(form, response) {
        var allowedHosts = response.allowedHosts;
        var checked = [];
        for (var i=0; i < allowedHosts.length; i = i + 1) {
            checked.push(allowedHosts[i].alias);
        }
        var field = this.getForm().findField('allowedHosts');
        field.setValue(checked);
        
        field = this.getForm().findField('supportsGbsTransport');
        if (!field.getValue()) {
            this.changeSbmPanel(false);
        }
        
        var conn = response.ftpConnection;
        if (conn) {
            this.getForm().findField('sbmName').setValue(conn.host);
            this.getForm().findField('sbmPort').setValue(conn.port);
            this.getForm().findField('sbmUser').setValue(conn.username);
            this.getForm().findField('sbmPassword').setValue(conn.plainPassword);
            this.getForm().findField('sbmDir').setValue(conn.directory);
            this.getForm().findField('sbmPassive').setValue(conn.passive);
            this.getForm().findField('sbmImplicit').setValue(conn.implicit);
            this.connectionid = conn.id;              
        }
        
        this.schedule.afterFormLoad(this.getForm(), response.scheduleExpressions);
    }
    
    ,changeSbmPanel: function(enable) {
        var that = this;
        var panel = this.items.find(function(i) {
            return i.id === ('form-tab-panel-'+that.id);
        });
        
        panel.items.each(function(item) {
            if (item.id == 'sbm-panel-'+that.id) {
                item.items.each(function (i) {
                     if (enable) {
                        i.enable();
                    } else {
                        i.disable();
                    }
                });
            }
        });

    }
       
    ,preSubmit: function(form, contentSet) {
        var field = this.getForm().findField('allowedHosts');
        var store = field.store;
        contentSet.allowedHosts = [];
        if (!field.getCheckedValue().blank()) {
            var aliases = field.getCheckedValue().split(',');
            var i, alias, entry;
            for (i=0; i < aliases.length; i = i + 1) {
                alias = aliases[i].trim();
                entry = store.getAt(store.find('alias', alias));
                
                contentSet.allowedHosts.push({
                    id: entry.get('id'),
                    alias: alias, 
                    commonName: entry.get('commonName')
                });
            }
        }
        contentSet.supplier = true;
        
        field = this.getForm().findField('supportsGbsTransport');
        
        if (field.getValue()) {
            var conn = {
                host: this.getForm().findField('sbmName').getValue(),
                port: this.getForm().findField('sbmPort').getValue(),
                username: this.getForm().findField('sbmUser').getValue(),
                directory: this.getForm().findField('sbmDir').getValue(),
                passive: this.getForm().findField('sbmPassive').getValue(),
                implicit: this.getForm().findField('sbmImplicit').getValue(),
                plainPassword: this.getForm().findField('sbmPassword').getValue()
            };             
                      
            if (this.connectionid !== undefined) {
                conn.id = this.connectionid;
            }
            contentSet.ftpConnection = conn;            
        }
        this.schedule.preSubmit(this.getForm(), contentSet);
        
        delete contentSet.sbmName;
        delete contentSet.sbmPort;
        delete contentSet.sbmUser;
        delete contentSet.sbmDir;
        delete contentSet.sbmPassive;
        delete contentSet.sbmImplicit;
        delete contentSet.sbmPassword;
    }
});




EFD.cs.ConsumerForm = Ext.extend(EFD.cs.ContentSetForm, {
 
    initComponent: function() {
        this.schedule = new EFD.cs.SchedulePanel({intervalLabel: 'Inventory Request Interval'}); 
        
        var storeInterval = new Ext.data.Store({reader: new Ext.ux.MapReader()});
        var storeFileFilter = new Ext.data.Store({reader: new Ext.ux.MapReader()});
        var storeFileSizeUnit = new Ext.data.Store({reader: new Ext.ux.MapReader()});
        var supplierStore = new Ext.data.Store({
            reader : new Ext.ux.ObjectReader({}, [
               {name: 'description'},
               {name: 'text'},
               {name: 'items'},
               {name: 'size'}
            ]),
            proxy: new Ext.ux.DWRProxy({
                dwrFunction: ContentSetHelper.listSuppliers,
                dwrFunctionArgs: function() {
                    var host = this.getForm().findField('supplierAddress').getValue();
                    var port = this.getForm().findField('supplierPort').getValue();
                    return [host, port];
                }.createDelegate(this)
            }),
            sortInfo:{field: 'text', direction: "ASC"}
        });
        
        var patternTypeCombo = new Ext.form.ComboBox({        
            hiddenName: 'patternType',
            store: storeFileFilter,
            displayField: 'value',
            valueField: 'key',
            mode: 'local',                         
            editable: false,
            minListWidth: 150,
            triggerAction: 'all',
            lazyInit: false,
            fieldLabel: 'Type'
        });
        
        storeFileFilter.on('load', function() {
           patternTypeCombo.setValue('B'); 
        });
        
        var deleteDelayCombo = new Ext.form.ComboBox({
            fieldLabel: 'File Delete Delay Unit',                            
            hiddenName: 'fileDeleteDelayUnit',
            store: storeInterval,
            displayField: 'value',
            valueField: 'key',
            mode: 'local',                         
            editable: false,
            anchor: '50%',
            triggerAction: 'all'                                                   
        });
        
        var fileSizeUnitCombo = new Ext.form.ComboBox({
            fieldLabel: 'Maximum File Size Unit',                            
            hiddenName: 'maxFileSizeUnit',
            store: storeFileSizeUnit,
            displayField: 'value',
            valueField: 'key',
            mode: 'local',
            editable: false,
            anchor: '50%',
            triggerAction: 'all'                                                   
        });
        
        storeInterval.loadData(EFD.util.constants.getTimeIntervals());
        storeFileFilter.loadData(EFD.util.constants.getFilterTypes());
        storeFileSizeUnit.loadData(EFD.util.constants.getFileSizeUnits());
        
        // configure ourselves
        Ext.apply(this, {
            saveMethod: ContentSetHelper.saveConsumer, 
            items: [{
                xtype: 'tabpanel',
                plain: true,
                activeTab: 0,
                // only fields from an active tab are submitted
                // if the following line is not persent
                deferredRender: false,
                height: 210,
                width: 800,
                defaults: {bodyStyle:'padding:10px'},
                items:[{
                    layout: 'form',
                    title: 'Details',
                    defaultType: 'textfield',
                    height: 90,
                    items:[{
                        fieldLabel: 'Name',
                        name:  'name',
                        allowBlank: false,
                        itemCls: 'required',                    
                        maxLength: 80,
                        regex: new RegExp('[a-zA-Z_0-9-]*'),
                        regexText: 'Name is only allow any combination of letter, digit, hyphen, or/and underscore',
                        anchor: '75%'
                    }, {
                        fieldLabel: 'Description',
                        name: 'description',
                        anchor:'95%'
                    }, {
                        fieldLabel: 'Content Set Directory',
                        name: 'path',
                        allowBlank: false,
                        itemCls: 'required',
                        anchor: '75%'            
                    }
                ]},this.schedule,{
                    title:'Supplier Details',
                    layout: 'form',                
                    defaultType: 'textfield',
                    items: [{                    
                        fieldLabel: 'Host Name or IP Address',
                        name: 'supplierAddress',
                        allowBlank: false,
                        itemCls: 'required',
                        anchor: '75%'
                    },{
                        xtype: 'numberfield',
                        fieldLabel: 'Port',
                        allowBlank: false,
                        allowDecimals: false,
                        allowNegative: false,
                        name: 'supplierPort',
                        itemCls: 'required',
                        anchor: '40%'                   
                    },{
                        xtype: 'combo',
                        fieldLabel: 'Name',                
                        name: 'supplierName',
                        loadingText: 'Retrieving Suppliers...',
                        id: 'supplier-select-'+this.id,
                        store: supplierStore,
                        displayField: 'text',
                        valueField: 'text',
                        anchor: '75%',
                        tpl: new Ext.XTemplate(
                            '<tpl for="."><div class="supplier-item">',
                                '<h3>{text}</h3>',
                                '{description}',
                                '<tpl if="description">. </tpl>',
                                '<tpl if="size === -1">Size information unavailable</tpl>',
                                '<tpl if="size !== -1">{items} items / {[EFD.util.formatBytes(values.size)]}</tpl>',
                            '</div></tpl>'),
                        itemSelector: 'div.supplier-item',
                        mode: 'remote',                         
                        editable: true,
                        forceSelection: false,
                        allowBlank: false,
                        triggerAction: 'all',
                        itemCls: 'required',
                        initQuery: Ext.emptyFn, // This ensures that the store won't loaded just by typing in the combo. See http://extjs.com/forum/showthread.php?p=295003#post295003
                        listeners: {
                            'beforequery': function() {
                                var host = this.getForm().findField('supplierAddress');
                                var port = this.getForm().findField('supplierPort');
                                
                                if (!host.isValid() || !port.isValid()) {
                                    EFD.error.errorHandler('Enter a valid host name and port.', {});
                                    return false;
                                }
                                return true;
                                
                            }.createDelegate(this)
                        },
                        beforeBlur: function() {
                            // This ensures that the "change" event gets fired if
                            // the user only types in the combo instead of selecting an item
                            Ext.form.ComboBox.prototype.beforeBlur.call(this);
                            this.setValue(this.el.dom.value);
                        }
                    }]
                },{
                    title:'Retrieval Options',
                    layout:'form',                
                    defaultType: 'numberfield',
                    items: [{
                        xtype: 'combo',
                        fieldLabel: 'Number of concurrent transfers',                           
                        hiddenName: 'concurrentTransfers',
                        store: ['1', '2'],
                        triggerAction: 'all',
                        forceSelection: true,
                        editable: false
                    },{                   
                        fieldLabel: 'Maximum File Size',
                        name: 'maxFileSize',
                        allowDecimals: false,
                        allowNegative: false,
                        anchor: '75%'                    
                    }, fileSizeUnitCombo, {
                        fieldLabel: 'File Delete Delay', 
                        name: 'fileDeleteDelay',
                        allowDecimals: false,
                        allowNegative: false,
                        anchor: '75%'                
                    }, deleteDelayCombo, {
                        xtype: 'checkbox',
                        fieldLabel: 'Use GBS Transport',
                        name: 'supportsGbsTransport',
                        disabled: (EFD.util.constants.isGbsEnabled() === false)
                    }]
                }, {                
                    title: 'Filter Options',
                    layout: 'form',
                    border: false,
                    labelAlign: 'right',
                    items: [{
                        name: 'useFilter',
                        xtype: 'checkbox',
                        fieldLabel: 'Use Filter',
                        listeners: {
                            check: function(field, checked) { 
                                this.showFilterConfig(checked);
                            }.createDelegate(this)
                        }
                    },{
                        xtype: 'fieldset',
                        title: 'Configuration',
                        id: 'filter-config-'+this.id,
                        autoHeight: true,
                        items: [patternTypeCombo,
                        {
                            xtype: 'textfield',
                            name: 'filterPattern',
                            fieldLabel: 'Pattern',
                            itemCls: 'required'
                        },{
                            xtype: 'radiogroup',
                            fieldLabel: '',
                            labelSeparator: '',
                            items: [
                                {boxLabel: 'Accept Only', name: 'filterAccept', inputValue: true},
                                {boxLabel: 'Exclude', name: 'filterAccept', inputValue: false, checked: true}
                            ]
                        }]
                    }]
                }, {
                     title: 'Integration Options',
                     layout: 'form',
                     border: false,
                     labelAlign: 'right',
                     items: [{
                        name: 'virtualManifest',
                        xtype: 'textfield',
                        fieldLabel: 'Virtual Manifest Path',
                        anchor: '75%'
                     }]
                }]
            }]
        }); // eo apply
        
        // Removing the lastQuery property allows the combo box to
        // be reloaded after an error or that it doesn't cache the results
        supplierStore.on('loadexception', function() {
            var combo = Ext.getCmp('supplier-select-'+this.id);
            combo.lastQuery = null;
        }.createDelegate(this));
         supplierStore.on('load', function() {
            var combo = Ext.getCmp('supplier-select-'+this.id);
            combo.lastQuery = null;
        }.createDelegate(this));
 
        EFD.cs.ConsumerForm.superclass.initComponent.apply(this, arguments);
        
        this.on('afterload', this.afterFormLoad, this);
        this.on('beforesave', this.preSubmit, this);
 
    } // eo function initComponent
    
    ,showFilterConfig: function(enable) {
        var that = this;
        var panel = this.items.find(function(i) { 
            return i.id === ('form-tab-panel-'+that.id);
        });
        
        var fieldset = Ext.getCmp('filter-config-'+this.id);
        
        fieldset.items.each(function(item) {
            if (enable) {
                item.enable();
            } else {
                item.disable();
            }
        });

    }
    
    ,formRendered: function() {
        if (!this.contentset) {
            this.schedule.formRendered(this.getForm());
            this.showFilterConfig(false);
        }
        EFD.cs.ConsumerForm.superclass.formRendered.apply(this, arguments);
    }
    
    ,afterFormLoad: function(form, response) {
        var f = this.getForm();
        
        if (response.filters.length > 0) {
            this.showFilterConfig(true);
            var filter = response.filters[0];
            
            f.findField('filterPattern').setValue(filter.pattern);
            f.findField('filterAccept').setValue(filter.inclusive+'');
            f.findField('patternType').setValue(filter.patternType);
            f.findField('useFilter').setValue(true);
            this.filterid = filter.id;
       } else {
            this.showFilterConfig(false);
       }
        
        this.schedule.afterFormLoad(f, response.scheduleExpressions);
    }
    
    ,preSubmit: function(form, contentSet) {
        var f = this.getForm();
        contentSet.filters = [];
        
        if (contentSet.useFilter === 'on') {
            var filter = {
                pattern: contentSet.filterPattern,
                inclusive: contentSet.filterAccept,
                patternType: contentSet.patternType
            };
            if (this.filterid !== undefined) {
                filter.id = this.filterid;
            }
            contentSet.filters.push(filter);
        }
        
        this.schedule.preSubmit(f, contentSet);
        
        delete contentSet.filterPattern;
        delete contentSet.filterAccept;
        delete contentSet.patternType;
        delete contentSet.useFilter;
    }
});
 
 
 
 
// register xtype
Ext.reg('consumerform', EFD.cs.ConsumerForm);
Ext.reg('supplierform', EFD.cs.SupplierForm);  
