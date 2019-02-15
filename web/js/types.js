/*global Ext, EFD, AlertHelper */

Ext.ns('EFD.types');

EFD.types.LinksPanel = Ext.extend(Ext.Panel, {

    initComponent:function() {
        
        Ext.apply(this, {
            title: 'Navigation',
            autoScroll: true,
            items: this.getItems.call(this),
            tbar: this.createToolbar.call(this)
            
        });
        EFD.types.LinksPanel.superclass.initComponent.apply(this, arguments);
    } // e/o initComponent
 
}); // e/o extend


EFD.types.MenuTip = Ext.extend(Ext.ToolTip, {
    
    autoScroll: true,
    hideDelay: 500,
    
    mouseOffset: [55, 15],
    frame: true,
    border: false,
    
    initComponent: function() {
        this.addEvents('showtip');
        this.on('showtip', this.show, this);
        
        EFD.types.MenuTip.superclass.initComponent.call(this);
        
        this.setTargetXY();
    }
    
    /**
     * Overriden from Ext.Tip to use the toolbar width instead of the body width
     * as the width size
     */
    ,doAutoWidth : function(){
        var bw = this.getTopToolbar().getEl().getTextWidth();
        
        if (this.title) {
            bw = Math.max(bw, this.header.child('span').getTextWidth(this.title));
        }
        
        bw += this.getFrameWidth() + (this.closable ? 20 : 0) + this.body.getPadding("lr");
        this.setWidth(bw.constrain(this.minWidth, this.maxWidth));
    }
    
    /**
     * The superclass onTargetOver overrides this.targetXY, we need to reset it.  We also ignore
     * mouseover events if we're already shown
     */
    ,onTargetOver : function(e) {
        if (this.shown) {
            return;
        }
        EFD.types.MenuTip.superclass.onTargetOver.call(this, e);
        this.setTargetXY();
    }
    
    /**
     * The superclass onMouseMove overrides this.targetXY, we need to reset it
     */
    ,onMouseMove : function(e) {
        EFD.types.MenuTip.superclass.onMouseMove.call(this, e);
        this.setTargetXY();
    }
    
    ,setTargetXY: function() {
        this.targetXY = [this.target.getX(), this.target.getY()];
    }
    
    ,afterRender: function() {
        EFD.types.MenuTip.superclass.afterRender.call(this);
        this.body.addClass('menu-tip');
        
        if (this.getTopToolbar()) {
            this.getTopToolbar().items.each(function(e) {
                var old = e.handler || Ext.emptyFn;
                var that = this;
                e.handler = function() { that.hide(); old();};
            },
            this);
        }
    }
    
    ,show : function() {
        if (this.shown) {
            return;
        }
        
        this.shown = true;
        
        EFD.types.MenuTip.superclass.show.call(this);

        this.body.on('mouseover', this.onTipOver, this);
        this.body.on('mouseout', this.onTipOut, this);
        
        if (this.getTopToolbar()) {
            this.getTopToolbar().getEl().on('mouseover', this.onTipOver, this);
            this.getTopToolbar().getEl().on('mouseout', this.onTipOut, this);
        }
        
    }
    
    ,onTipOver: function(e) {
        this.clearTimer('dismiss');
        this.clearTimer('hide');
    }
    
    ,onTipOut: function(e) {
        this.delayHide();
    }
    
    ,hide: function() {
        this.shown = false;
        EFD.types.MenuTip.superclass.hide.call(this);
    }
    
    ,getTargetXY : function(){
        return [this.target.getX()+this.mouseOffset[0], this.target.getY()+this.mouseOffset[1]];
    }
});




EFD.types.DWRTreePanel = Ext.extend(Ext.tree.TreePanel, {
    
    initComponent: function() {
        Ext.apply(this, {
            animate: true,
            containerScroll: true,
            lines: false,
            border: false,
            selModel: new Ext.tree.MultiSelectionModel({}),
            loader: new Ext.ux.DWRTreeLoader({
                dwrCall: this.dwrCall,
                baseAttrs: this.baseAttrs
            }),
            root: new Ext.ux.AsyncTreeNode({
                text: this.treeTitle,
                hasChildren: true,
                draggable: false,
                expandable: true
            })
        });
        
        this.on('aftersave', this.refresh, this);
        
        EFD.types.DWRTreePanel.superclass.initComponent.apply(this, arguments);
        
        // By default, sorter sorts "asc" on the "text" property 
        var treeSorter = new Ext.tree.TreeSorter(this, {property: this.sortProperty});
        
    } // e/o initComponent
    
    ,refresh: function(formpanel, contentSet) {
        var refresh = null;
        if (this.shouldRefresh) {
            refresh = this.shouldRefresh.call(this, contentSet);
        }
        if (refresh) {
            this.getRootNode().reload();
        }
    }
 
 
}); // e/o extend



EFD.types.Window = Ext.extend(Ext.Viewport, {

    layout: 'border',
    closable: false,
    
    initComponent:function() {
        var tmpl = new Ext.XTemplate('<div class="classification" style="background-color: {color}">{level}</div>');
        var classifcationHtml = tmpl.apply( {
            color: this.banner.backgroundColor,
            level: this.banner.securityLevel
        });
        delete this.banner;

        var win = Ext.getCmp('window-viewport');
        win.addListener('afterlogin', function(){
            var statusbar = Ext.getCmp('user-statusbar');
            statusbar.addButton({
                xtype: 'button',
                cls: 'logout-button',
                iconCls: 'logout-icon',
                text: 'Logout',
                listeners: {
                    click: EFD.login.logout.createDelegate(this, [true])
                }
            });
        }.createDelegate(this));
        
        Ext.apply(this, {
            items: [{
                xtype: 'panel',
                layout: 'fit',
                region: 'center',
                id: 'window-panel',
                border: false,
                items: [this.mainPanel],
                bbar: new Ext.StatusBar({
                    defaultText: '',
                    id: 'user-statusbar'
                })
            }, {
                
                xtype: 'panel',
                region: 'north',
                height: 55,
                border: false,
                items: [{
                    border: false,
                    html: classifcationHtml
                },{
                    border: false,
                    contentEl: 'header'
                }]
            },{
                region: 'south',
                border: false,
                html: classifcationHtml
            }]
        }); // e/o apply
     
        // call parent
        EFD.types.Window.superclass.initComponent.apply(this, arguments);
        
        this.addEvents('afterlogin', 'afterlogout');
        
        this.on('afterlogin', function(user) {
            this.user = user;
            var sb = Ext.getCmp('user-statusbar');
            
            var name = this.user.firstName+' '+this.user.lastName;
            var date = this.user.lastLogin && this.user.lastLogin.toDTG() || '&lt;unavailable&gt;';
            var host = this.user.lastLocation || '&lt;unavailable&gt;';
            var failed = this.user.lastFailedLogins;
            
            var txt = 'Welcome {name}.  Last login on {date} from {host}';
            
            if (failed > 0) {
                txt = txt + '. There were <span class="bold">{failed}</span> failed logins since your last login';
            }
            
            var tmpl = new Ext.XTemplate(txt);
            
            sb.setStatus({
                text: tmpl.apply({
                    name: name,
                    date: date,
                    host: host,
                    failed: failed
                })
            });             
        }.createDelegate(this));
        
        this.on('afterlogout', function() {                
            delete this.user;  
        }.createDelegate(this));
    } // e/o function initComponent
    
}); // e/o extend

EFD.types.Panel = Ext.extend(Ext.Panel, {

    layout: 'border',
    border: false,
    
    initComponent: function() {
        Ext.apply(this, {
            items: this.getItems()
        });
        
        EFD.types.Panel.superclass.initComponent.apply(this, arguments);
        
        this.tabPanel = Ext.getCmp('window-tab-panel');
    } // e/o function initComponent
    
    ,afterRender: function() {
        EFD.types.Panel.superclass.afterRender.call(this);
    }
    
    ,getItems: function() {
        
        return [{ 
                xtype:'linkspanel',
                user: this.user,
                region: 'west',
                width: 200,
                split: true,
                border: true,
                collapsible: true,
                id: 'linkspanel',
                listeners: {
                    afterlayout: this.setupLinksPanel,
                    scope: this,
                    single: true
                },
                getItems: this.getLinkPanelItems.createDelegate(this),
                createToolbar: this.createLinkPanelToolbar.createDelegate(this)
                },{
                xtype: 'tabpanel',
                id: 'window-tab-panel',
                region: 'center',
                border: false,
                activeItem: 0,
                enableTabScroll: true,
                listeners: {
                    add: function(tp, tab, index) {
                        tab.on('formchanged', function() {
                            if (tab.title.lastIndexOf('*') !== (tab.title.length-1)) {
                                tab.setTitle(tab.title+' *');
                            }
                        });
                    }
                },
                items: [{
                    frame: true,
                    title:'Welcome',

                    items: [{
                        contentEl: 'welcome',
                        style: 'padding-bottom: 10px; padding-top: 10px'
                    },{
                        xtype: 'alertpanel',
                        autoScroll: true,
                        user: this.user
                    }]
                }],
                plugins: new Ext.ux.TabCloseMenu()
        }];
    }
    
    ,createLinkPanelToolbar: function() {
        return [];
    }
    
    
    
}); // e/o extend


EFD.types.AlertPanel = Ext.extend(Ext.Panel, {

    border: false,
    
   
    initComponent:function() {

        var reader = new Ext.data.JsonReader({
                root: 'page',
                totalProperty: 'count',
                id: 'id'
            },[
            {name: 'id'},
            {name: 'type'},
            {name: 'timestamp'},
            {name: 'unread'},
            {name: 'message'}
        ]);
        
        var func = AlertHelper.getUserAlerts;
        if (this.user.adminRole) {
            func = AlertHelper.getAdminAlerts;
        }
        
        delete this.user;

        var alertsStore = new Ext.data.Store({
            proxy: new Ext.ux.DWRProxy({
                dwrFunction: func
            }),
            reader: reader
        });
        
        var expander = new Ext.grid.RowExpander({
            tpl : new Ext.XTemplate([
                '<p><b>Date:</b> {[EFD.util.renderers.DATE(values.timestamp)]}</p><br>',
                '<p>{message}</p>']
            ),
            rowClass: function(record) {
                if (record.get('unread')) {
                    return 'alert-unread';
                }
                return '';
            }
        });
        
        expander.on('expand', function(expander, record, body, rowIndex) {
            if (record.get('unread')) {
                var row = expander.grid.view.getRow(rowIndex);
                Ext.get(row).removeClass('alert-unread');
                AlertHelper.markRead(record.get('id'));
                record.set('unread', false);
            }
            return true;
        });
        
        var columns = [
            expander,
            {header: "Date",  width: 40, sortable: true, dataIndex: 'timestamp', renderer: function(value) {
                return value.getMonth()+1+"/"+value.getDate()+"/"+value.getFullYear();
            }},
            {header: "Message",  sortable: true, dataIndex: 'message'}
        ];

        var grid = new Ext.grid.GridPanel({
            style: 'padding: 4px',
            store: alertsStore,
            columns: columns,
            stripeRows: true,
            loadMask: true,
            height: 400,
            frame: true,
            plugins: expander,
            title: EFD.current.getUser().adminRole ?'User Alerts':'Synchronization Alerts',
            viewConfig: {
                autoFill: true
            },
            bbar: new Ext.ux.PagingToolBar({
                pageSize: EFD.util.constants.getPageSize(),
                store: alertsStore,
                displayInfo: true,
                displayMsg: 'Displaying alerts {0} - {1} of {2}',
                emptyMsg: 'No alerts to display'
            })
        });

        var menu = new Ext.menu.Menu([{
            text: 'Delete',
            id: 'delete-alert',
            iconCls: 'delete-icon',
            handler: function() {
                this.deleteAlerts(grid, grid.getSelectionModel().getSelections());
            }.createDelegate(this)
        }]);
    
        grid.on('rowcontextmenu', function(grid, rowIndex, event) {
            event.stopEvent();

            if(!grid.getSelectionModel().isSelected(rowIndex)) {
                grid.getSelectionModel().selectRow(rowIndex, true, false);
            }

            menu.showAt(event.getPoint());
        });
        
        grid.on('keypress', function(event) {
            if (event.getCharCode() === event.DELETE) {
                this.deleteAlerts(grid, grid.getSelectionModel().getSelections());
            }
        }.createDelegate(this));
        
        Ext.apply(this, {
            items: [grid]
        });
        
        alertsStore.load({
            params: {
                start: 0, 
                limit: EFD.util.constants.getPageSize()
            }
        });
        
        EFD.types.AlertPanel.superclass.initComponent.apply(this, arguments);
    },
    
    deleteAlerts: function(grid, records) {
        grid.suspendEvents();
        
        var idsToDelete = records.map(function(record){
                return record.get('id'); 
        });
        AlertHelper.remove(idsToDelete, {
            callback: function() {
                //After the delete call returns, reload the grid store
                grid.store.load({
                    params: {
                        start: grid.getBottomToolbar().cursor,
                        limit: EFD.util.constants.getPageSize()
                    },
                    callback: function(){
                       //Resume grid events after the store is reloaded
                       grid.resumeEvents();
                    }.createDelegate(this)
                });
            }
        });
    }
    
});

EFD.types.MainPanel = Ext.extend(Ext.Panel, {
    
    layout: 'card',
    activeItem: 0,
    deferredRender: false,
    border: false,
    
    initComponent: function() {
        var items = null;
        if (this.user.adminRole) {
            items = this.getAdminItems();
            this.addAdminNavbar();
        } else {
            items = this.getUserItems();
            this.addUserNavbar();
        }
        
        items = items.concat(this.getCommonItems());
        
        this.addCommonNavbar();
        
        var bar = Ext.get('navigation');
        bar.show(true);
        
        Ext.apply(this, {
            items: items
        });
        
        EFD.types.MainPanel.superclass.initComponent.apply(this, arguments);
    }
    
    ,highlightNavbarItem: function(obj) {
        Ext.each(Ext.query("#navigation a"), 
            function (item, index, allItems) {
                Ext.get(item).removeClass("navigation-selected");
            }
        );
        obj.addClass("navigation-selected");
    }
    
    ,showNavigator: function() {
        this.getNavBarFunction('navigator')();
    }
    
    ,addCommonNavbar: function() {
        this.addNavbarItem('Help', this.getNavBarFunction('help'));
        this.addNavbarItem('Change Password', function() { 
            var x = new EFD.login.ChangePasswordWindow({showSuccess: true, logout: false}); 
            x.show();
        });
    }
    
    ,addAdminNavbar: function() {
        this.addNavbarItem('Navigator', this.getNavBarFunction('navigator'));
    }
    
    ,addUserNavbar: function() {
        this.addNavbarItem('Navigator', this.getNavBarFunction('navigator'));
        this.addNavbarItem('Dashboard', this.getNavBarFunction('dashboard'));
    }
    
    ,getAdminItems: function() {
        return [{
            xtype: 'adminpanel',
            user: this.user,
            navKey: 'navigator'
        }];
    }
    
    ,getUserItems: function() {
        return [{
            xtype: 'userpanel',
            user: this.user,
            navKey: 'navigator'
        },{
            xtype: 'dashboard',
            autoScroll: true,
            split: true,
            navKey: 'dashboard'
        }];
    }
    
    ,getCommonItems: function() {
        return [{
            autoScroll: true,
            layout: 'fit',
            contentEl: (this.user.adminRole ? 'admin-help' : 'user-help'),
            navKey: 'help'
        }];
    }
    
    ,addNavbarItem: function(text, clickHandler) {
        var e = Ext.DomHelper.append('navigation', { 
            tag: 'a', 
            href: '#' + EFD.login.normalizeHistoryToken(text), 
            id: 'navigation-' + EFD.login.normalizeHistoryElementId(text), 
            html: text 
        }, true);
        //Don't navigate in this window if the user shift (new window) or control (new tab) clicks
        e.on('click', function(evt){ 
            if (!evt.hasModifier()) {
                clickHandler(); 
                evt.stopEvent();
            }
        });
    }

    ,showPanel: function(key) {
        if (key !== 'change+password') {
            if (['navigator','dashboard','help'].indexOf(key) === -1) {
                //If the key can't be identified, load the navigator
                key = 'navigator';
            }
            var anchor = Ext.get('navigation-'+EFD.login.normalizeHistoryElementId(key));
            this.highlightNavbarItem(anchor);
            this.getLayout().setActiveItem(this.items.findIndex('navKey', key));
        } else {
            //Handle the case where someone bookmarks or Ctrl-clicks the change password link
            //Since we're not supporting /#change+password yet, redirect to /#navigator, changing the url
            //When change password is its own panel, this condition will be removed.
            Ext.History.add('navigator');
        }
    }
    
    ,getNavBarFunction: function(key){
        return function(){
            Ext.History.add(key);
        };
    }
    
});

Ext.reg('alertpanel', EFD.types.AlertPanel);
Ext.reg('dwrtree', EFD.types.DWRTreePanel);
Ext.reg('linkspanel', EFD.types.LinksPanel);
