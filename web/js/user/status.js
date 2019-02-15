/*global Ext, EFD, StatusService */
Ext.ns('EFD.status');

EFD.status.BaseStatusPanel = Ext.extend(Ext.Panel, {
    
    autoScroll: true,
    refreshPeriod: 3000,
    doUpdate: Ext.emptyFn,
    getStatusBarText: Ext.emptyFn,
    getStatusBarConfig: Ext.emptyFn,
    layout: 'column',
    border: true,
    
    initComponent: function() {
        this.statusTpl = new Ext.XTemplate(this.getStatusBarText());
        this.statusTpl.compile();
        
        Ext.apply(this, {
            bbar: new Ext.StatusBar({
                defaultText: 'Loading...',
                id: 'status-statusbar-'+this.contentset.id
            })
        });
        
        EFD.status.BaseStatusPanel.superclass.initComponent.apply(this, arguments);
        
        this.on('nodedeleted', this.remove, this);
        this.on('aftersave', this.changeTitle, this);
        this.on('beforedestroy', this.clearTimeout.createDelegate(this));
        Ext.getCmp('window-tab-panel').on('tabchange', this.tabChanged.createDelegate(this));
    }
    
    ,changeTitle: function(panel, contentSet) {
        if (typeof contentSet == 'object') {
            if (this.contentset.id == contentSet.id) {
                this.setTitle(contentSet.name+' - Status');
            }
        }
    }
    
    ,remove: function(panel, contentSet) {
        if (this.contentset.id == contentSet.id) {
            Ext.getCmp('window-tab-panel').remove(this);
        }
    }
    
    ,tabChanged: function(tabpanel, tab) {
        if (tab === this) {
            this.doLoad();
        } else {
            this.clearTimeout();
        }
    }
    
    ,clearTimeout: function() {
        if (this.timeout) {
            window.clearTimeout(this.timeout);
            delete this.timeout;
        }
        return true;
    }
    
    ,doLoad: function() {
        this.statusMethod.call(this, this.contentset.id, {callback: this.doRender.createDelegate(this)});
    }
    
    ,doRender: function(status) {
        this.status = status;
        this.updateStatusBar(status);
        this.doUpdate(status);
        this.timeout = this.doLoad.defer(this.refreshPeriod, this);
    }
    
    ,updateStatusBar: function(status) {
        var sb = Ext.getCmp('status-statusbar-'+this.contentset.id);
        if (sb) {
            var txt = this.statusTpl.apply(this.getStatusBarConfig(status));
            sb.setStatus({
                text: txt
            });
        }
    }
    
});

EFD.status.SupplierStatusPanel = Ext.extend(EFD.status.BaseStatusPanel, {
    
    statusMethod: StatusService.getSupplierStatus,
    
    initComponent: function() {
        
        this.activeRenderer = function(value, meta, record, rowIndex) {
            if (record.get('active')) {
                var currentTransfers = record.get('currentTransfers');
                if (currentTransfers.length > 0) {
                    var names = [];
                    for (var i=0; i < currentTransfers.length; i = i + 1) {
                        names.push(currentTransfers[i].fileName);
                    }
                    return 'Sending: '+names.join(',');
                } else {
                    return 'Waiting for consumer';
                }
            } else {
                var result = record.get('result');
                return result.message;
            }
        };
        
        this.itemRenderer = function(value, meta, record) {
            return record.get('itemsCompleted') + ' / ' + record.get('totalItems');
        };
        
        this.byteRenderer = function(value, meta, record) {
            return EFD.util.formatBytes(record.get('completedBytes')) + ' / ' + EFD.util.formatBytes(record.get('totalBytes'));
        };
        
        var reader = new Ext.ux.ObjectReader({}, [
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
            {name: 'currentTransfers'},
            {name: 'percentComplete'},
            {name: 'fileName'},
            {name: 'bytesTransferred'},
            {name: 'result'}
        ]);
        
        this.supplierStore = new Ext.data.Store({
            reader: reader,
            sortInfo:{field: 'active', direction: "DESC"}
        });
        
        var supplierGrid = this.getGrid(this.supplierStore, 'Current Synchronizations');
        
        Ext.apply(this, {
            items:[{
				cls:'status-panels',
				border: false,
                items: [supplierGrid]
            }]
        });
        
        EFD.status.SupplierStatusPanel.superclass.initComponent.apply(this, arguments);
    }
    
    ,getStatusBarConfig: function(status) {
        return {
            status: status.state,
            next: status.nextEstimatedRuntime <= 0 ? 'Unavailable' : EFD.util.formatTime(status.nextEstimatedRuntime),
            last: status.lastRuntime === -1 ? 'Unavailable' : new Date(status.lastRuntime).toDTG(),
            size: status.itemCount === -1 ? 'Unavailable' : status.itemCount+' items / '+EFD.util.formatBytes(status.size)
        };
    }
    
    ,getStatusBarText: function() {
        return  '<span class="bold">State:</span> {status}.  ' +
                '<span class="bold">Last ran on:</span> {last}.  ' +
                '<span class="bold">Will run again in:</span> {next}.  ' +
                '<span class="bold">Size:</span> {size}';
    }
    
    ,getGrid: function(store, title) {
        var columns = [
            {header: "Status", sortable: true, width: 55, dataIndex: 'active', renderer: this.activeRenderer.createDelegate(this)},
            {header: "Host", sortable: true, dataIndex: 'host'},
            {header: "Date", sortable: true, width: 120, dataIndex: 'date', renderer: EFD.util.renderers.DATE},
            {header: "Items", sortable: true, dataIndex: 'itemsCompleted', renderer: this.itemRenderer},
            {header: "Bytes", sortable: true, dataIndex: 'completedBytes', renderer: this.byteRenderer},
            {header: "% Done", sortable: true, width: 50, dataIndex: 'percentSuccessful', renderer: function(value) {return value + '%';}}
        ];
        
        return new Ext.grid.GridPanel({
            autoScroll : true,
            store: store,
            columns: columns,
            viewConfig: {
                forceFit: true
           },
            stripeRows: true,
            border: true,
            title: title,
            autoHeight: true,
            width: 800
        });
    }    
    
    ,doUpdate: function(status) {
        this.supplierStore.loadData(status.currentSynchronizations);
    }
    
});

EFD.status.ConsumerStatusPanel = Ext.extend(EFD.status.BaseStatusPanel, {
    
    statusMethod: StatusService.getConsumerStatus,
    
    initComponent: function() {
        var detailsTplTxt = '<table class="status-details">'+
        '<thead><tr><th>{title}</th></tr></thead>' +
        '<tbody>'+
        '<tr class="even"><td>Added / Remaining</td><td>{adds}</td></tr>' +
        '<tr class="odd"><td>Updated / Remaining</td><td>{updates}</td></tr>' +
        '<tr class="even"><td>Deleted / Remaining</td><td>{deletes}</td></tr>' +
        '<tr class="odd"><td>Failed</td><td>{failed}</td></tr>'+
        '</tbody>' +
        '</table>';
        
        this.detailsTpl = new Ext.XTemplate(detailsTplTxt);
        this.detailsTpl.compile();
        
        Ext.apply(this, {
            
            items: [{
            	xtype: 'panel',
            	border: false,
				columnWidth: '.6',
            	items: [{
                    xtype: 'panel',
					cls:'status-panels',
					title: 'Overall Progress',
                	items: [{
                    	xtype: 'progress',
                    	id: 'overall-progress-bar-'+this.contentset.id,
                    	cls: 'status-bar'
                	}],
                	bbar: new Ext.StatusBar({})
        	   	},{
                    xtype: 'grid',
                    id: 'current-file-grid-'+this.contentset.id,
                    autoScroll : true,
                    store: new Ext.data.Store({
                        reader: new Ext.ux.ObjectReader({}, [
                            {name: 'percentComplete'},
                            {name: 'bytesTransferred'},
                            {name: 'bytesRequested'},
                            {name: 'fileName'}
                        ]),
                        sortInfo:{field: 'fileName', direction: "DESC"}
                    }),
                    columns: [
                        {header: "File", sortable: true, dataIndex: 'fileName'},
                        {header: "Bytes", sortable: true, dataIndex: 'bytesTransferred', renderer: function(value, meta, record) {
                            return EFD.util.formatBytes(record.get('bytesTransferred')) + ' / ' + EFD.util.formatBytes(record.get('bytesRequested'));
                        }},
                        {header: "% Done", sortable: true, width: 50, dataIndex: 'percentComplete', renderer: function(value) {return value + '%';}}
                    ],
                    viewConfig: {
                        forceFit: true
                   },
                    stripeRows: true,
                    border: true,
                    title: 'Current Files',
                    autoHeight: true,
                    cls: 'status-panels'
                }]
            },{
                xtype: 'panel',
				cls:'status-panels',
				columnWidth: '.4',
                title: 'Details',
                id: 'details-panel-'+this.contentset.id,
                tbar: [{
                    xtype: 'cycle',
                    showText: true,
                    prependText: 'Displaying ',
                    items: [{
                        text:'items',
                        checked: true,
                        id: 'show-items'
                    },{
                        text:'bytes',
                        id: 'show-bytes'
                    }],
                    changeHandler: this.changeDetails.createDelegate(this)
                }]
            }]
        });
        
        EFD.status.ConsumerStatusPanel.superclass.initComponent.apply(this, arguments);
    }
    
    ,getStatusBarConfig: function(status) {
        return {
            status: status.state,
            elapsed: status.elapsedTime === -1 ? 'Unavailable' : EFD.util.formatTime(status.elapsedTime),
            remaining: status.remainingTime === -1 ? 'Unavailable' : EFD.util.formatTime(status.remainingTime),
            next: status.nextEstimatedRuntime <= 0 ? 'Unavailable' : EFD.util.formatTime(status.nextEstimatedRuntime),
            last: status.lastRuntime === -1 ? 'Unavailable' : new Date(status.lastRuntime).toDTG(),
            size: status.itemCount === -1 ? 'Unavailable' : status.itemCount+' items / '+EFD.util.formatBytes(status.size)
        };
    }
    
    ,getStatusBarText: function() {
        return  '<span class="bold">State:</span> {status}.  ' +
                '<span class="bold">Running for:</span> {elapsed}.  ' +
                '<span class="bold">Est. time remaining:</span> {remaining}.  ' +
                '<span class="bold">Last ran on:</span> {last}.  ' +
                '<span class="bold">Will run again in:</span> {next}.  ' +
                '<span class="bold">Size:</span> {size}';
    }
    
    ,doUpdate: function(status) {
        this.changeDetails(null, {id: this.details || 'show-items'});
        this.updateOverallProgress(status);
        this.updateCurrentFileProgress(status);
    }
    
    ,updateOverallProgress: function(status) {
        var bar = Ext.getCmp('overall-progress-bar-'+this.contentset.id);
        if (bar) {
            var value = status.percentCompleted / 100;
            bar.updateProgress(value, status.percentCompleted+'%');
        }
    }
    
    ,updateCurrentFileProgress: function(status) {
        var grid = Ext.getCmp('current-file-grid-'+this.contentset.id);
        if (!grid) {
            return;
        }
        grid.getStore().loadData(status.currentTransfers);
    }
    
    ,changeDetails: function(btn, item) {
        if (item.id === 'show-bytes') {
            this.showBytes();
        } else {
            this.showItems();
        }
    }
    
    ,showBytes: function(status) {
        status = status || this.status;
        this.details = 'show-bytes';
        this.applyDetails({
            title: 'Bytes',
            adds: EFD.util.formatBytes(status.bytesAdded)+' / '+EFD.util.formatBytes(status.bytesAddedRemaining),
            updates: EFD.util.formatBytes(status.bytesUpdated)+' / '+EFD.util.formatBytes(status.bytesUpdatedRemaining),
            deletes: EFD.util.formatBytes(status.bytesDeleted)+' / '+EFD.util.formatBytes(status.bytesDeletedRemaining),
            failed: EFD.util.formatBytes(status.bytesFailed)
        });
    }
    
    ,showItems: function(status) {
        status = status || this.status;
        this.details = 'show-items';
        this.applyDetails({
            title: 'Items',
            adds: status.itemsAdded+' / '+status.itemsAddedRemaining,
            updates: status.itemsUpdated+' / '+status.itemsUpdatedRemaining,
            deletes: status.itemsDeleted+' / '+status.itemsDeletedRemaining,
            failed: status.failures
        });
    }
    
    ,applyDetails: function(config) {
        var cmp = Ext.getCmp('details-panel-'+this.contentset.id);
        if (cmp) {
            var el = cmp.body;
            var markup = this.detailsTpl.apply(config);
            el.update(markup);
        }
    }
    
});

Ext.reg('supplierstatuspanel', EFD.status.SupplierStatusPanel);
Ext.reg('consumerstatuspanel', EFD.status.ConsumerStatusPanel);
