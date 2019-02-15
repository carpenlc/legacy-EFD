/*global Ext, EFD, UserHelper */

Ext.ns('EFD.admin');

EFD.admin.UserForm = Ext.extend(Ext.ux.DWRForm, {
    border: false,
    frame: true,
    labelWidth: 80,
    autoScroll: true,
    
    initComponent: function() {
        Ext.apply(this, {
        	layout: "form",
        	saveMethod: UserHelper.saveUser, 
            labelAlign: 'right',
            labelWidth: 250,
            bodyStyle:'padding:5px',
            width: 600,
            defaultType: 'textfield',
            items: [{
                fieldLabel: "Username",
                name: "username",
                allowBlank: false,
                itemCls: 'required'
            },{
            	fieldLabel: "First Name",
            	name: "firstName",
            	allowBlank: false,
            	itemCls: 'required'
            },{
            	fieldLabel: "Last Name",
            	name: "lastName",
            	allowBlank: false,
            	itemCls: 'required'
            },{
                fieldLabel: "Password",
                name: "password",
                allowBlank: this.user ? true : false,
                itemCls: this.user ? '' : 'required',
                inputType: "password",
                id: "password-"+this.getId()
            },{
                fieldLabel: "Confirm Password",
                name: "passwordConfirm",
                allowBlank: this.user ? true : false,
                itemCls: this.user ? '' : 'required',
                inputType: "password",
                vtype: 'password',
                initialPassField: "password-"+this.getId()
            },{
            	fieldLabel: "Administrator",
            	name: "adminRole",
            	xtype: 'checkbox'
            },{
            	fieldLabel: "Enabled",
            	name: "enabled",
            	xtype: 'checkbox'
            }],
            buttons: [{
                text: 'Save',
                handler: this.submit,
                scope: this
            }]
        });
        
        EFD.admin.UserForm.superclass.initComponent.apply(this, arguments);
        
        this.on('formrendered', this.formRendered, this);
        this.on('beforesave', this.prepareUser, this);
        this.on('aftersave', this.afterSave, this);
        this.on('beforeload', this.beforeFormLoad, this);
        this.on('statuschanged', this.updateStatus, this);
        this.on('nodedeleted', this.remove, this);
    }
    
    ,updateStatus: function(panel, user) {
        if (this.user) {
            if (this.user.id == user.id) {
                var f = this.getForm().findField('enabled');
                if (!user.enabled) {
                    f.setValue('on');
                } else {
                    f.setValue(false);
                }
            }
        }
    }
    
    ,remove: function(panel, user) {
        if (this.user) {
            if (this.user.id == user.id) {
                Ext.getCmp('window-tab-panel').remove(this);
            }
        }
    }
    
    ,beforeFormLoad: function(form, user) {
        EFD.util.copyProperties(user, this.user, ['initialPassword', 'lastFailedLogins', 'lastLogin', 'lastLocation', 'currLogin', 'location', 'failedLogins']);
    }

	,prepareUser: function(form, user, args) {
		if (this.user) {
            EFD.util.copyProperties(this.user, user, ['id', 'initialPassword', 'lastFailedLogins', 'lastLogin', 'lastLocation', 'currLogin', 'location', 'failedLogins']);
        }
        
        user.enabled = (user.enabled === 'on');
        user.adminRole = (user.adminRole === 'on');
        
        args.push(user.password);
        args.push(user.passwordConfirm);
        
        delete user.passwordConfirm;
        delete user.password;
	}

	,formRendered: function(user) {
        if (this.user) {
            this.onLoad();
        } else {
            this.loadMask.hide();
            this.on('aftersave', this.afterNewSave, this);
        }
	}
	
    ,onLoad: function() {
        var callback = this.doFormLoad.createDelegate(this);
        var eh = function() {
            this.loadMask.hide();
        }.createDelegate(this);
        UserHelper.getUserByName(this.user.username, {callback: callback, postHook: eh});
    }
    
    ,afterSave: function(id, user) {
        EFD.util.msg(user.username, 'Saved');
        this.setTitle(user.username);
    }
    
    ,afterNewSave: function(form, user, id) {
        this.user = {
            id: id
        };
        
        this.un('aftersave', this.afterNewSave, this);
    }
    
});

Ext.reg('userform', EFD.admin.UserForm);
