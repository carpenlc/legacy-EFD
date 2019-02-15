/*global Ext, EFD, ContentSetHelper */

Ext.ns('EFD.user');

EFD.user.UserPanel = Ext.extend(EFD.types.Panel, {
    
    id: 'userPanel', // used by the dashboard to get reference to tabs to open tabs for status and history
    
    initComponent: function() {
       this.addEvents('pathupdated', 'statuschanged', 'nodedeleted');
       EFD.user.UserPanel.superclass.initComponent.call(this);
    }
    
    
    
    ,getLinkPanelItems: function() {
        return [{
            xtype:'dwrtree',
            treeTitle: 'Suppliers',
            dwrCall: ContentSetHelper.getSuppliers,
            enableDD: true,
            ddGroup: 'dd-suppliers',
            shouldRefresh: function(cs) { return cs === true || cs.supplier; }
        },{
            xtype:'dwrtree',
            treeTitle: 'Consumers',
            dwrCall: ContentSetHelper.getConsumers,
            enableDD: true,
            ddGroup: 'dd-consumers',
            shouldRefresh: function(cs) { return cs === true || !cs.supplier; }
        }];
    }
    
    ,createLinkPanelToolbar: function() {
        var items = EFD.user.UserPanel.superclass.createLinkPanelToolbar.call(this);
        return [{
            iconCls:'new-icon',
            tooltip: 'Create a new content set...',
            id: 'new-cs-button',
            menu: {
                items: [{
                    id: 'new-consumer-button',
                    text: 'New Consumer',
                    iconCls:'new-consumer-icon',
                    handler: this.createConsumer.createDelegate(this)
                }, {
                    id: 'new-supplier-button',
                    text: 'New Supplier',
                    iconCls:'new-supplier-icon',
                    handler: this.createSupplier.createDelegate(this)
                }]
            }
        },{
            id:'delete-cs-button',
            iconCls:'delete-icon',
            tooltip: 'Delete the selected content sets',
            handler: this.multiDelete.createDelegate(this)
        },{
            id:'enable-cs-button',
            iconCls:'enable-cs-icon',
            tooltip: 'Enable the selected content sets',
            handler: this.multiEnable.createDelegate(this)
        },{
            id:'disable-cs-button',
            iconCls:'disable-cs-icon',
            tooltip: 'Disable the selected content sets',
            handler: this.multiDisable.createDelegate(this)
        },{
            iconCls: "x-tbar-loading",
            tooltip: 'Reload content sets',
            handler: this.refreshTrees.createDelegate(this)
        }].concat(items);
    }
    
    ,refreshTrees: function() {
        var panel = Ext.getCmp('linkspanel');
        panel.items.each(function(tree){
            tree.getRootNode().reload();
        });
    }
    
    /**
     * Callback after the linkspanel is laid out.
     *
     * We add all the necessary event handlers to the items in the panel
     */
    ,setupLinksPanel: function(linksPanel, layout) {
        var tpl = new Ext.XTemplate('<div class="menu-tip"><dl><dt>Last updated on:</dt><dd>{date}</dd><dt>Description:</dt><dd>{desc}</dd><dt>Status:</dt><dd>{status}</dd></dl></div>');
        
        var that = this;
        var tree;
        var root;
        var i;
        
        for (i=0; i < linksPanel.items.length; i = i + 1) {
            tree = linksPanel.items.itemAt(i);
            root = tree.getRootNode();
            
            // Relay "aftersave" from this to the tree
            tree.relayEvents(this, ['aftersave']);
            tree.on('dragdrop', this.nodeDropped.createDelegate(this));
            
            var rootMenu = new Ext.menu.Menu([{
                text: 'Expand all',
                id: 'expand',
                iconCls: 'expand-icon',
                handler: function(node) { node.expand(true); }.createDelegate(this, [root])
            },{
                text: 'Collapse all',
                id: 'collapse',
                iconCls: 'collapse-icon',
                handler: function(node) { node.collapse(true); }.createDelegate(this, [root])
            },
            new Ext.menu.Separator({}),{
                text: 'New Folder',
                iconCls: 'new-folder-icon',
                handler: this.createNewFolder.createDelegate(this, [root])
            }]);
        
            root.on('contextmenu', function(menu) {
                return function(node, evt) {
                    menu.showAt(evt.getPoint());
                };
            }(rootMenu));
            
            root.on('childrenrendered', function(tree) { 
                return function(node) {
                    var nodes = [].concat(node.childNodes);
                    var child;
                    var contentset;
                    var menu;
                    var tip;
                    
                    while (nodes.length > 0) {
                        child = nodes.shift();
                        
                        if (!child.leaf) {
                            nodes = nodes.concat(child.childNodes);
                            continue;
                        }
                        contentset = that.convertContentSet(child);
                        
                        if (contentset.displayPath && contentset.displayPath !== '/') {
                            that.setupFolders(tree.getRootNode(), child, contentset);
                        }
                        
                        if (contentset.enabled) {
                            child.getUI().addClass('enabled');
                        } else {
                            child.getUI().addClass('disabled');
                        }
                        
                        menu = new Ext.menu.Menu([{
                            text: 'Edit',
                            iconCls: 'edit-icon',
                            handler: that.editContentSet.createDelegate(that, [contentset])
                        },{
                            text: 'Status',
                            iconCls: 'status-icon',
                            handler: that.showStatus.createDelegate(that, [contentset])
                        },{
                            text: 'History',
                            iconCls: 'history-icon',
                            handler: that.showHistory.createDelegate(that, [contentset])
                        },{
                            text: contentset.enabled ? 'Disable' : 'Enable',
                            iconCls: contentset.enabled? 'disable-cs-icon' : 'enable-cs-icon',
                            handler: that.changeStatus.createDelegate(that, [tree, contentset])
                        },{
                            text: 'Delete',
                            iconCls: 'delete-icon',
                            handler: that.deleteContentSet.createDelegate(that, [tree, contentset])
                        }]);
                        
                        tip = new Ext.ToolTip({
                            target: child.getUI().getTextEl(),
                            html: tpl.apply({
                                desc: (child.attributes.description || '&lt;none&gt;').limit(50), 
                                date: (child.attributes.date === 0 ? 'N/A' : new Date(child.attributes.date).toDTG()), 
                                status: contentset.enabled ? 'Enabled' : 'Disabled'
                            })
                        });
                    
                        child.on('contextmenu', function(menu) {
                            return function(node, evt) {
                                menu.showAt(evt.getPoint());
                            };
                        }(menu));
                    }
                };
            }(tree));
        }
    }
    
    ,setupFolders: function(root, child, contentSet) {
        var parts = contentSet.displayPath.split('/');
        
        var parent = root;
        var created;
        var path;
        
        for (var i=0; i < parts.length; i++) {
            if (!parts[i].blank()) {
                created = this.createFolderNode(parent, parts[i]);
                if (parent.indexOf(created) === -1) {
                    parent.appendChild(created);
                    created.render();
                }
                parent = created;
            }
        }
        
        parent.appendChild(child);
        parent.render();
        parent.renderChildren();
        parent.renderIndent(true, true);
    }
    
    ,nodeDropped: function(tree, node, dd, evt) {
        var that = this;
        if (!node.isLeaf()) {
            node.childNodes.each(function(child) {
                that.nodeDropped(tree, child, dd, evt);
            });
            return;
        }
        
        var path = node.getPath('text');
        path = path.substring(tree.getRootNode().getPath('text').length);
        path = path.substring(0, path.length - node.attributes.text.length);
        
        var old = node.attributes.displayPath;
        
        if (path !== old) {
            if (old || path !== '/') {
                ContentSetHelper.updateDisplayPath(path, node.attributes.id);
                this.fireEvent('pathupdated', this, node.attributes.id, path);
            }
        }
    }
    
    ,createFolderNode: function(node, txt) {
        var e = node.findChild('text', txt);
        if (e) {
            return e;
        }
        
        var child = new Ext.tree.TreeNode({
            text: txt,
            leaf: false,
            expandable: true
        });
        var menu = new Ext.menu.Menu([{
            text: 'Expand all',
            id: 'expand',
            iconCls: 'expand-icon',
            handler: function() { child.expand(true); }
        },{
            text: 'Collapse all',
            id: 'collapse',
            iconCls: 'collapse-icon',
            handler: function() { child.collapse(true); }
        },
        new Ext.menu.Separator({}),
        {
            text: 'New Folder',
            iconCls: 'new-folder-icon',
            handler: this.createNewFolder.createDelegate(this, [child])
        },{
            text: 'Delete',
            id: 'delete',
            iconCls: 'delete-icon',
            handler: function() { child.parentNode.removeChild(child); }
        }]);
        
        child.on('contextmenu', function(menu) {
            return function(node, evt) {
                menu.item('id', 'delete').setDisabled((node.childNodes.length > 0));
                menu.showAt(evt.getPoint());
            };
        }(menu));
        return child;
    }
    
    ,createNewFolder: function(parent) {
        var func = function(btn, txt) {
            if (btn === 'ok' && !txt.blank()) {
                var node = this.createFolderNode(parent, txt);
                if (parent.indexOf(node) === -1) {
                    parent.appendChild(node);
                    node.render();
                }
            }        
        };
        
        Ext.MessageBox.prompt('Add Folder', 'Name', func, this);
    }
    
    ,convertContentSet: function(node) {
        return {
            id: node.attributes.id,
            name: node.attributes.text,
            supplier: node.attributes.supplier,
            enabled: node.attributes.enabled,
            displayPath: node.attributes.displayPath
        };
    }
    
    ,changeStatus: function(tree, contentSet) {
        var msg = {
            title: contentSet.name,
            msg: 'Successfully ' + (contentSet.enabled ? 'disabled ' : 'enabled ')
        };
        
        var data = {
            callback: this.statusChanged.createDelegate(this, [tree, msg]) 
        };
        
        if (contentSet.enabled) {
            ContentSetHelper.disable([contentSet.id], data);
        } else {
            ContentSetHelper.enable([contentSet.id], data);
        }
        
        this.fireEvent('statuschanged', this, contentSet);
    }
    
    ,deleteContentSet: function(tree, contentSet) {
        EFD.util.ask('Confirm Delete', 'Are you sure you want to delete '+contentSet.name+'?', this.confirmDeleteContentSet.createDelegate(this, [tree, contentSet]));
    }

    ,confirmDeleteContentSet: function(tree, contentSet) {
        var msg = {
            title: contentSet.name,
            msg: 'Successfully deleted ' + (contentSet.name)
        };
           var data = {
            callback: this.statusChanged.createDelegate(this, [tree, msg]) 
           };
        ContentSetHelper.remove([contentSet.id], data);
        this.fireEvent('nodedeleted', this, contentSet);
    }
    
    ,statusChanged: function(tree, msg) {
        tree.getRootNode().reload();
        EFD.util.msg(msg.title, msg.msg);
    }
    
    ,editContentSet: function(contentset) {
        var xtype = 'consumerform';
        if (contentset.supplier) {
            xtype = 'supplierform';
        }
        
        var tab = this.showTab(contentset, xtype, contentset.name, 'edit-icon');
        if (tab) {
            // Relay "aftersave" events from the tab to this
            this.relayEvents(tab, ['aftersave']);
        }
    }
    
    ,showHistory: function(contentset) {
        var xtype = 'consumersyncpanel';
        if (contentset.supplier) {
            xtype = 'suppliersyncpanel';
        }
        
        var tab = this.showTab(contentset, xtype, contentset.name + ' - History', 'history-icon');
        if (tab) {
            // Relay "aftersave" events from the this to the tab
            tab.relayEvents(this, ['aftersave']);
        }
    }
    
    ,showStatus: function(contentset) {
        var xtype = 'consumerstatuspanel';
        if (contentset.supplier) {
            xtype = 'supplierstatuspanel';
        }
        
        var tab = this.showTab(contentset, xtype, contentset.name + ' - Status', 'status-icon');
        if (tab) {
            // Relay "aftersave" events from the this to the tab
            tab.relayEvents(this, ['aftersave']);
        }
    }
    
    ,showTab: function(contentset, xtype, title, iconCls) {
        var tab = this.tabPanel.items.find(function(i){return i.title === title;});
        if (!tab) {
            tab = this.tabPanel.add({
                title: title,
                activate: true,
                contentset: contentset,
                xtype: xtype,
                iconCls: iconCls,
                closable: true
            });
             
            this.tabPanel.setActiveTab(tab);
            tab.doLayout();
            
            // Relay events from this to the tab
            tab.relayEvents(this, ['pathupdated', 'statuschanged', 'nodedeleted']);
            
            return tab;
        } else {
            this.tabPanel.setActiveTab(tab);
        }
    }
    
    ,createConsumer: function() {
        this.addContentSetForm('consumerform', 'New Consumer', ContentSetHelper.newConsumer);
    }
    
    ,createSupplier: function() {
        this.addContentSetForm('supplierform', 'New Supplier', ContentSetHelper.newSupplier);
    }
    
    ,addContentSetForm: function(xtype, title, template) {
        var tab = this.tabPanel.add({
            title: title,
            activate: true,
            border: false,
            xtype: xtype,
            closable: true,
            iconCls: 'edit-icon',
            template: template 
        });
        
        // Relay "aftersave" events from the tab to this
        this.relayEvents(tab, ['aftersave']);
        
        // Relay events from this to the tab
        tab.relayEvents(this, ['pathupdated', 'statuschanged', 'nodedeleted']);
             
        this.tabPanel.setActiveTab(tab);
        tab.doLayout();
    }
    
    ,getSelectedContentSets: function(selector) {
        var panel = Ext.getCmp('linkspanel');
        var sets = [];
        var selectedSets;
        var that = this;
        var i;
        var c;
        
        selector = selector || function() { return true; };
        
        panel.items.each(function(tree) {
            selectedSets = tree.getSelectionModel().getSelectedNodes();
            for (i=0; i < selectedSets.length; i = i + 1) {
                if (selectedSets[i].isLeaf()) {
                    c = that.convertContentSet(selectedSets[i]);
                    if (selector(c)) {
                        sets.push(c);
                    }
                }
            }
        });
        return sets;
    }
    
    ,getIdsFromSets: function(sets) {
        var i;
        var ids = [];
        for (i=0; i < sets.length; i = i + 1) {
            ids.push(sets[i].id);
        }
        return ids;
    }
    
    ,multiDelete: function() {
        var sets = this.getSelectedContentSets();
        
        if (sets.length === 0) {
            EFD.util.msg('Error', 'No content sets selected.');
            return;
        }
        
        this.multiAction(sets, 'delete', 'Deleting...', 'Deleted', ContentSetHelper.remove, 'nodedeleted');
    }
    
    ,multiEnable: function() {
        var sets = this.getSelectedContentSets(function(c){return c.enabled === false;});
        
        if (sets.length === 0) {
            EFD.util.msg('Error', 'No disabled content sets selected.');
            return;
        }
        
        this.multiAction(sets, 'enable', 'Enabling...', 'Enabled', ContentSetHelper.enable, 'statuschanged');
    }
    
    ,multiDisable: function() {
        var sets = this.getSelectedContentSets(function(c){return c.enabled === true;});
        
        if (sets.length === 0) {
            EFD.util.msg('Error', 'No enabled content sets selected.');
            return;
        }
        
        this.multiAction(sets, 'disable', 'Disabling...', 'Disabled', ContentSetHelper.disable, 'statuschanged');
    }
    
    ,multiAction: function(sets, action, waitMsg, successMsg, func, eventName) {
        var txt = '<p>Are you sure you want to '+action+' the following?</p>';
        txt = txt + '<div>Content Sets: </div><ul class="confirm"><tpl for="sets"><li>{name}</li></tpl></ul>';
        
        var callback = this.doMultiAction.createDelegate(this, [sets, waitMsg, successMsg, func, eventName]);      
        
        var tpl = new Ext.XTemplate(txt);
        EFD.util.ask('Confirm', tpl.apply({sets: sets}), callback);
    }
    
    ,doMultiAction: function(sets, waitMsg, successMsg, func, eventName) {
        var ids = this.getIdsFromSets(sets);
        
        var mask = new Ext.LoadMask(this.getEl(), {msg: waitMsg, removeMask: true});
        mask.show();
        var success = function() {
            mask.hide();
            EFD.util.msg('Success', successMsg);
            this.fireEvent('aftersave', this, true);
            for (var i=0; i < sets.length; i++) {
                this.fireEvent(eventName, this, sets[i]);
            }
        };
        
        var postHook = function() {
            mask.hide();
        };
        
        func.call(this, ids, {
            callback: success.createDelegate(this),
            postHook: postHook.createDelegate(this)
        });
    }
    
}); // e/o extend

Ext.reg('userpanel', EFD.user.UserPanel);
