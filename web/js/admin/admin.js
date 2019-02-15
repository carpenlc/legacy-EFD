/*global Ext, EFD, AllowedHostService, AdminHelper, UserHelper */

Ext.ns('EFD.admin');

EFD.admin.AdminPanel = Ext.extend(EFD.types.Panel, {
    
    createLinkPanelToolbar: function() {
        var items = EFD.admin.AdminPanel.superclass.createLinkPanelToolbar.call(this);
        var that = this;
        return [{
        	id: 'admin-new-button',
            iconCls:'new-icon',
            tooltip: 'Create new...',
            menu: {
                items: [{
                    id: 'new-allowed-host-button',
                    text: 'New Allowed Host',
                    iconCls: 'new-allowed-host-icon',
                    handler: this.createNewAllowedHost.createDelegate(this)
                },{
                    id: 'new-user-button',
                    text: 'New User',
                    iconCls: 'new-user-icon',
                    handler: this.createNewUser.createDelegate(this)
                }]
            }
        }, {
        	id: 'admin-delete-button',
            iconCls: 'delete-icon',
            tooltip: 'Delete selected...',
            handler: this.multiDelete.createDelegate(this)
        },{
        	id: 'admin-reload-button',
            iconCls: "x-tbar-loading",
            tooltip: 'Reload users and allowed hosts',
            handler: this.refreshTrees.createDelegate(this)
        }].concat(items);
    }
    
    ,refreshTrees: function() {
        var panel = Ext.getCmp('linkspanel');
        panel.items.each(function(tree){
            tree.getRootNode().reload();
        });
    }

    ,getLinkPanelItems: function() {
    	var that = this;
        return [{
            xtype: 'dwrtree',
            id: 'allowed-hosts-tree',
            treeTitle: 'Allowed Hosts',
            dwrCall: AllowedHostService.list,
            sortProperty: 'alias',
            baseAttrs: {leaf: true},
            shouldRefresh: function(arg) { return arg === true || arg.hasOwnProperty('alias'); }
        },{
            xtype: 'dwrtree',
            id: 'users-tree',
            treeTitle: 'Users',
            dwrCall: UserHelper.getUsers,
            shouldRefresh: function(arg) { return arg === true || arg.hasOwnProperty('username'); }
        }];
    }
    
    ,setupLinksPanel:  function(linksPanel, layout) {
    	var that = this;
        this.setupUserTree(Ext.getCmp('users-tree'));
        this.setupAllowedHosts(Ext.getCmp('allowed-hosts-tree'));
    }
    
    ,setupAllowedHosts: function(tree) {
        var tpl = new Ext.XTemplate('<div class="menu-tip"><span class="bold">Common Name:</span> {name}</dl></div>');
        
        var root = tree.getRootNode();
        var that = this;
        
        // Relay "aftersave" from this to the tree
        tree.relayEvents(this, ['aftersave']);
        
        // The allowed host objects doesn't have a "text" attribute
        // which the TreePanel uses to display the node.
        // We need to update the text of the nodes after they are rendered
        root.on('childrenrendered', function(tree) { 
            return function(node) {
                for (var j=0; j < node.childNodes.length; j = j + 1) {
                    var child = node.childNodes[j];
                    child.setText(child.attributes.alias);
                    
                    var tip = new Ext.ToolTip({
                        target: child.getUI().getTextEl(),
                        html: tpl.apply({
                            name: child.attributes.commonName
                        })
                    });
                    
                    var menu = new Ext.menu.Menu([{
                        text: 'Edit',
                        iconCls: 'edit-icon',
                        handler: that.editAllowedHost.createDelegate(that, [child])
                    },{
                        text: 'Delete',
                        iconCls: 'delete-icon',
                        handler: that.deleteAllowedHost.createDelegate(that, [child])
                    }]);
                    
                    var cb = function(menu) {
                        return function(node, evt) {
                            menu.showAt(evt.getPoint());
                        };
                    }(menu);
                    
                    child.on('contextmenu', cb);
                }
            };
       }(tree));
    }
    
    ,setupUserTree: function(tree) {
        var tpl = new Ext.XTemplate('<div class="menu-tip"><dl><dt>Name:</dt><dd>{name}</dd><dt>Last logged in:</dt><dd>{date}</dd><dt>Failed Logins:</dt><dd>{logins}</dd><dt>Status:</dt><dd>{enabled}</dd></dl></div>');
        
        var root = tree.getRootNode();
        var that = this;
        
        // Relay "aftersave" from this to the tree
        tree.relayEvents(this, ['aftersave']);
        
        root.on('childrenrendered', function(tree) { 
            return function(node) {
                for (var j=0; j < node.childNodes.length; j = j + 1) {
                    var child = node.childNodes[j];
                    var target = child.getUI().getTextEl();
                    
                    var user = that.convertUser(child);
                    
                    var date = child.attributes.lastLogin;
                    date = date ? date.toDTG() : '&lt;Unavailable&gt;';
                    
                    var tip = new Ext.ToolTip({
                        target: target,
                        html: tpl.apply({
                            name: child.attributes.name,
                            enabled: user.enabled ? 'Enabled' : 'Disabled',
                            logins: child.attributes.failedLogins, 
                            date: date
                        })
                    });
                    
                    var edit_menu_item = {
                            text: 'Edit',
                            iconCls: 'edit-icon',
                            handler: that.editUser.createDelegate(that, [user])
                        };
                    
                    var status_menu_item = {
                            text: user.enabled ? 'Disable' : 'Enable',
                                    iconCls: user.enabled ? 'disable-cs-icon' : 'enable-cs-icon',
                                    handler: that.changeStatus.createDelegate(that, [tree, user])
                                };
                    
                    var delete_menu_item = {
                            text: 'Delete',
                            iconCls: 'delete-icon',
                            handler: that.deleteUser.createDelegate(that, [user])
                        };
                    
                    var full_menu = new Ext.menu.Menu([edit_menu_item, status_menu_item, delete_menu_item]);
                    
                    //truncated menu for own account
                    //do not show disable/delete 
                    var truncated_menu = new Ext.menu.Menu([edit_menu_item]);
                    	
                    var menu_function = function(menu) {
                        return function(node, evt) {
                            menu.showAt(evt.getPoint());
                        };
                    };
                    
                    //full
                    var cb = menu_function(full_menu);
                    //truncated
                    var tcb = menu_function(truncated_menu);
                    
                    if (EFD.current.getUser().username == user.username) {
                    	child.on('contextmenu', tcb);
                    } else {
                    	child.on('contextmenu', cb);
                    }
                }
            };
       }(tree));
    }
    
    ,editUser: function(user) {
        var tab = this.showTab({user: user}, 'userform', user.username, 'edit-icon');
        if (tab) {
            // Relay "aftersave" events from the tab to this
            this.relayEvents(tab, ['aftersave']);
            
            // Relay events from this to the tab
            tab.relayEvents(this, ['statuschanged', 'nodedeleted']);
        }
    }
    
    ,changeStatus: function(tree, user) {
    	if (user.username == EFD.current.getUser().username) {
    		EFD.error.defaultHandler('You cannot change your own status.','');
    		return;
    	}
    	
        var msg = {
            title: user.username,
            msg: 'Successfully ' + (user.enabled ? 'disabled ' : 'enabled ')
        };
        
        var data = {
            callback: this.statusChanged.createDelegate(this, [tree, msg, user]) 
        };
        
        if (user.enabled) {
            AdminHelper.disable([user.id], data);
        } else {
            AdminHelper.enable([user.id], data);
        }
    }
    
    ,statusChanged: function(tree, msg, user) {
        tree.getRootNode().reload();
        EFD.util.msg(msg.title, msg.msg);
        this.fireEvent('statuschanged', this, user);
    }
    
    ,editAllowedHost: function(node) {
        var allowedHost = this.convertAllowedHost(node);
        
        var tab = this.showTab({allowedHost: allowedHost}, 'allowedhostform', allowedHost.alias, 'edit-icon');
        if (tab) {
            // Relay "aftersave" events from the tab to this
            this.relayEvents(tab, ['aftersave']);
            
            // Relay events from this to the tab
            tab.relayEvents(this, ['nodedeleted']);
        }
    }
    
    ,convertAllowedHost: function(node) {
        return {
            alias: node.attributes.alias,
            commonName: node.attributes.commonName,
            id: node.attributes.id
        };
    }
    
    ,convertUser: function(node) {
        return {
            id: node.attributes.id,
            username: node.attributes.text,
            enabled: node.attributes.enabled
        };
    }
    
    ,deleteAllowedHost: function(node) {
        var allowedHost = this.convertAllowedHost(node);
        
        EFD.util.ask('Confirm Delete', 'Are you sure you want to delete '+allowedHost.alias+'?', this.confirmDeleteAllowedHost.createDelegate(this, [allowedHost]));
    }
    
    ,deleteUser: function(user) {
    	if (user.username == EFD.current.getUser().username) {
    		EFD.error.defaultHandler('You cannot delete your own account.','');
    	} else {
    		EFD.util.ask('Confirm Delete', 'Are you sure you want to delete '+user.username+'?', this.confirmDeleteUser.createDelegate(this, [user]));
    	}
    }
    
    ,confirmDeleteAllowedHost: function(allowedHost) {
        var mask = new Ext.LoadMask(this.getEl(), {msg: 'Deleting...', removeMask: true});
        mask.show();
        var success = function() {
            mask.hide();
            EFD.util.msg(allowedHost.alias, 'Deleted');
            this.fireEvent('aftersave', this, allowedHost);
            this.fireEvent('nodedeleted', this, allowedHost);
        };
        
        var postHook = function() {
            mask.hide();
        };
        
        AllowedHostService.remove(allowedHost, {
            callback: success.createDelegate(this),
            postHook: postHook.createDelegate(this)
        });
    }
    
    ,confirmDeleteUser: function(user) {
        var mask = new Ext.LoadMask(this.getEl(), {msg: 'Deleting...', removeMask: true});
        mask.show();
        var success = function() {
            mask.hide();
            EFD.util.msg(user.username, 'Deleted');
            this.fireEvent('aftersave', this, user);
            this.fireEvent('nodedeleted', this, user);
        };
        
        var postHook = function() {
            mask.hide();
        };
        
        AdminHelper.deleteAll([], [user], {
            callback: success.createDelegate(this),
            postHook: postHook.createDelegate(this)
        });
    }
    
    ,multiDelete: function() {
        var i;
        var userTree = Ext.getCmp('users-tree');
        var hostsTree = Ext.getCmp('allowed-hosts-tree');
        var hosts = [];
        var users = [];
        
        var selectedHosts = hostsTree.getSelectionModel().getSelectedNodes();
        for (i=0; i < selectedHosts.length; i = i + 1) {
            if (selectedHosts[i].isLeaf()) {
                hosts.push(this.convertAllowedHost(selectedHosts[i]));
            }
        }
        
        var selectedUsers = userTree.getSelectionModel().getSelectedNodes();
        for (i=0; i < selectedUsers.length; i = i + 1) {
        	var isLeaf = selectedUsers[i].isLeaf();
        	if (isLeaf) {
        		var convertedUser = this.convertUser(selectedUsers[i]);
        		if (convertedUser.username != EFD.current.getUser().username) {
        			users.push(convertedUser);
        		}
            }
        }
        
        if (hosts.length === 0 && users.length === 0) {
            EFD.util.msg('Error', 'No users or allowed hosts selected.');
            return;
        }
        
        var txt = '<p>Are you sure you want to delete the following?</p>';
        
        if (hosts.length >0) {
            txt = txt + '<div>Hosts: </div><ul class="confirm"><tpl for="hosts"><li>{alias}</li></tpl></ul>';
        }
        
        if (users.length > 0) {
            txt = txt + '<div>Users: </div><ul class="confirm"><tpl for="users"><li>{username}</li></tpl></ul>';
        }
        
        
        var callback = this.doMultiDelete.createDelegate(this, [hosts, users]);
        
        var tpl = new Ext.XTemplate(txt);
        EFD.util.ask('Confirm Delete', tpl.apply({hosts: hosts, users: users}), callback);
    }
    
    ,doMultiDelete: function(hosts, users) {
        var mask = new Ext.LoadMask(this.getEl(), {msg: 'Deleting...', removeMask: true});
        mask.show();
        var success = function() {
            mask.hide();
            EFD.util.msg('Success', 'Deleted');
            this.fireEvent('aftersave', this, true);
            var i;
            for (i=0; i < users.length; i = i + 1) {
                this.fireEvent('nodedeleted', this, users[i]);
            }
            for (i=0; i < hosts.length; i = i + 1) {
                this.fireEvent('nodedeleted', this, hosts[i]);
            }
        };
        
        var postHook = function() {
            mask.hide();
        };
        
        AdminHelper.deleteAll(hosts, users, {
            callback: success.createDelegate(this),
            postHook: postHook.createDelegate(this)
        });
    }
    
    ,showTab: function(config, xtype, title, iconCls) {
        var tab = this.tabPanel.items.find(function(i){return i.title === title;});
        if (!tab) {
            Ext.apply(config, {
                title: title,
                activate: true,
                xtype: xtype,
                iconCls: iconCls,
                closable: true
           });
            tab = this.tabPanel.add(config);
             
            this.tabPanel.setActiveTab(tab);
            tab.doLayout();
            return tab;
        } else {
            this.tabPanel.setActiveTab(tab);
        }
    }
    
    ,createNewUser: function() {
        var tab = this.tabPanel.add({
            title: 'New User',
            activate: true,
            xtype: 'userform',
            closable: true,
            iconCls: 'edit-icon'
        });
        
        // Relay "aftersave" events from the tab to this
        this.relayEvents(tab, ['aftersave']);
        
        // Relay events from this to the tab
        tab.relayEvents(this, ['statuschanged', 'nodedeleted']);
             
        this.tabPanel.setActiveTab(tab);
        tab.doLayout();
    }
    
    ,createNewAllowedHost: function() {
        var tab = this.tabPanel.add({
            title: 'New Allowed Host',
            activate: true,
            xtype: 'allowedhostform',
            closable: true,
            iconCls: 'edit-icon'
        });
        
        // Relay "aftersave" events from the tab to this
        this.relayEvents(tab, ['aftersave']);
        
        // Relay events from this to the tab
        tab.relayEvents(this, ['nodedeleted']);
             
        this.tabPanel.setActiveTab(tab);
        tab.doLayout();
    }
});

Ext.reg('adminpanel', EFD.admin.AdminPanel);
