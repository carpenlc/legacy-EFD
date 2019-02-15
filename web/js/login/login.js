/*global Ext, EFD, System, Static */

Ext.ns('EFD.login');

EFD.login.start = function() {
    Ext.QuickTips.init();
    Static.getData({callback: EFD.login.systemReady});
};

EFD.login.systemReady = function (data) {
    Ext.get(document.body).show(true);
    
    var banner = data.banner;
    delete data.banner;
    
    EFD.util.constants.init(data);
    
    document.title = banner.hostName +' - Enterprise File Delivery';
    
    EFD.login.initializeHistory();
    
    var win = new EFD.types.Window({
        id: 'window-viewport',
        banner: banner,
        mainPanel: new EFD.login.LoginPanel({})
    });
    win.show();
};

EFD.login.logout = function(prompt) {
    var doLogout = function() {
    	EFD.current.setUser(null);
        System.logout();
		var win = Ext.getCmp('window-viewport');
        win.fireEvent('afterlogout');
        window.location = '/';
    };
    
    if (prompt) {
        EFD.util.ask('Logout', 'Click OK to log out', doLogout);
    } else {
        doLogout();
    }
};

EFD.login.LoginPanel = Ext.extend(Ext.Panel, {

    initComponent: function() {
        Ext.apply(this, {
            autoScroll: true,
            border: false,
            items: [{
                xtype: 'panel',
                border: false,
                items: [{
                    xtype: 'form',
                    id: 'login-form',
                    frame: true,         
                    buttonAlign: 'center',                    
                    style: 'margin: 200px auto;',
                    title: 'Log In',
                    width: 280,
                    autoHeight: true,
                    defaultType: 'textfield',
                    items: [{
                        fieldLabel: 'User Name',
                        name: 'username'
                    },{
                        fieldLabel: 'Password',
                        name: 'password',
                        inputType: 'password',
                        listeners: {
                            specialkey: function(field, evt) {
                                if (evt.getKey() === Ext.EventObject.ENTER) {
                                    this.tryLogin();
                                }
                            }.createDelegate(this)
                        }
                    }],
                    buttons: [{
                        text: 'Log in',
                        id: 'login-button',
                        handler: this.tryLogin.createDelegate(this),
                        scope: this
                    },{
                        text: 'Reset',
                        handler: function() {this.getForm().reset(); },
                        scope: this
                    }],
                    listeners: {
                        afterlayout: function(formPanel) {
                            formPanel.getForm().findField('username').focus(true, 100);
                        }
                    }
                }]
            }]
        });
        
        if (EFD.error.message.isSet()) {
            this.items.unshift({
                border: false,
                html: '<div id="error">'+EFD.error.message.get()+'</div>'
            });
            EFD.error.message.clear();
        }
        
        EFD.login.LoginPanel.superclass.initComponent.apply(this, arguments);
    }
    
    ,afterRender: function() {
        EFD.login.LoginPanel.superclass.afterRender.apply(this, arguments);
        
        this.loadMask = new Ext.LoadMask(this.getEl(), {msg: 'Logging in...'});
    }
    
    ,getForm: function() {
        return Ext.getCmp('login-form').getForm();
    }
    
    ,tryLogin: function() {
        //Disable the form, suppressing login button clicks and enter key events 
        //during login
        if(!Ext.getCmp('login-form').disabled) {
            Ext.getCmp('login-form').disable();
        } else {
            //Login is in progress
            return;
        }
        var username = this.getForm().findField('username').getValue();
        var password = this.getForm().findField('password').getValue();
        
        this.getForm().findField('password').setValue('');
        this.loadMask.show();
        
        var posthook = function(msg, ex) {
            this.loadMask.hide();
        }.createDelegate(this);
        
        var errorHandler = function(msg, ex) {
            EFD.error.defaultHandler(msg,ex);
            //Re-enable the login-form only if there is an error
            Ext.getCmp('login-form').enable();
        };
        
        System.login(username, password, {
            callback: this.successfulLogin.createDelegate(this),
            postHook: posthook,
            errorHandler: errorHandler
        });
    }
    
    ,successfulLogin: function(user) {
    	EFD.current.setUser(user);
    	this.showBanner();
    }
    
    ,showBanner: function() {
        this.loadMask.hide();
        
        var confirm = new Ext.Window({
            modal: true,
            closable: false,
            title: 'DOD Notice and Consent',
            width: 500,
            autoScroll: true,
            buttonAlign: 'center',
            id: 'banner-window',
            contentEl: 'dod_banner',
            buttons: [{
                text: 'OK',
                id: 'dod-banner-ok',
                handler: this.bannerOK.createDelegate(this)
            }, {
                text: 'Cancel',
                handler: this.bannerCancel.createDelegate(this)
            }]
        });
        confirm.show(this.getEl());
    }
    
    ,bannerOK: function() {
        this.closeBanner();
        
        var win = Ext.getCmp('window-viewport');
        win.fireEvent('afterlogin', EFD.current.getUser());
        
        var wp = Ext.getCmp('window-panel');
        wp.items.each(function(i) {
            wp.remove(i, true);
        });
        
        wp.add(new EFD.types.MainPanel({
            id: 'main-panel',
            user: EFD.current.getUser()
        }));
        
        wp.doLayout();
        
        //Invoke the change event manually here to allow login to happen.   
        //Ext.History's change event is not fired automatically onload
        EFD.login.initializeHistoryFromCurrentLocation();

        EFD.heartbeat.start();
    }
    
    ,bannerCancel: function() {
        this.closeBanner();
        EFD.login.logout(false);
    }
    
    ,closeBanner: function() {
        var win = Ext.getCmp('banner-window');
        win.close();
    }
});


EFD.login.ChangePasswordWindow = Ext.extend(Ext.Window, {
    
    title: 'Change Your Password',
    modal: true,
    closable: false,
    id: 'change-pw-window',
    width: 280,
    
    initComponent: function() {
        Ext.apply(this, {
            
            items: [{
                xtype: 'panel',
                autoScroll: true,
                items: [{
                    xtype: 'form',
                    id: 'change-pw-form',
                    frame: true,
                    buttonAlign: 'center',
                    autoHeight: true,
                    defaultType: 'textfield',
                    items: [{
                        fieldLabel: 'Current Password',
                        name: 'pw',
                        inputType: 'password',
                        allowBlank: false
                    },{
                        fieldLabel: 'New Password',
                        name: 'newpw',
                        inputType: 'password',
                        id: 'newpw',
                        allowBlank: false
                    },{
                        fieldLabel: 'New Password (confirm)',
                        name: 'pwconfirm',
                        inputType: 'password',
                        vtype: 'password',
                        initialPassField: 'newpw',
                        allowBlank: false
                    }],
                    buttons: [{
                        text: 'OK',
                        handler: this.sendRequest.createDelegate(this)
                    },{
                        text: 'Cancel',
                        handler: this.close.createDelegate(this)
                    }],
                    listeners: {
                        afterlayout: function(formPanel) {  
                            formPanel.getForm().findField('pw').focus(false, 100);
                            formPanel.getForm().clearInvalid();
                        }
                    }
                }]
            }]
            
        });
        
        EFD.login.ChangePasswordWindow.superclass.initComponent.apply(this, arguments);
    }
    
    ,sendRequest: function() {
        var form = Ext.getCmp('change-pw-form').getForm();
        
        var pw = form.findField('pw').getValue();
        var newpw = form.findField('newpw').getValue();
        var newpwconfirm = form.findField('pwconfirm').getValue();
        System.changePassword(pw, newpw, newpwconfirm, {callback: this.passwordChanged.createDelegate(this)});
       
    }
    
    ,passwordChanged: function() {
        this.close();
        if (this.showSuccess === true) {
            EFD.util.msg('Success', 'Your password was changed');
        }
        if (this.logout === true) {
            EFD.login.logout(false);
        }
    }
});

/**
* Creates the required DOM for Ext.History
* Sets up a listener on Ext.History's change event to fire 
* EFD.login.handleHistoryChange
*/
EFD.login.initializeHistory = function() {
    var  historyForm = Ext.getBody().createChild({
        tag: 'form',
        action: '#',
        cls: 'x-hidden',
        id: 'history-form',
        children: [{
            tag: 'div',
            children: [{
                tag: 'input',
                id: Ext.History.fieldId,
                type: 'hidden'
            },{
                tag: 'iframe',
                id: Ext.History.iframeId
            }]
        }]
    });

    //initialize History management
    Ext.History.init();
    Ext.History.on('change', EFD.login.handleHistoryChange, this);
};

/**
 * Callback for history change events
 * @param token
 */
EFD.login.handleHistoryChange = function(token){
    //Currently the only supported history tokens are:
    //navigator, dashboard, help, and change+password,
    //all of which are handled by the mainPanel.
    var mainPanel = Ext.getCmp('main-panel');
    if (mainPanel) {
        //Only handle history events after the app is properly initialized
        mainPanel.showPanel(token);
    }
};

EFD.login.normalizeHistoryToken = function(token) {
	var normalizedToken = 'undefined';
	if (typeof token === 'string') {
		normalizedToken = token.toLowerCase().replace(' ', '+');
	}
	return normalizedToken;
};

EFD.login.normalizeHistoryElementId = function(id) {
	var normalizedId = EFD.login.normalizeHistoryToken(id).replace('+', '_');
	return normalizedId;
};

EFD.login.initializeHistoryFromCurrentLocation = function() {
    var token = document.location.hash.replace("#", "");
    if (token.length === 0) {
        //For urls with no history token, send user to navigator
        token = 'navigator';
    }
    EFD.login.handleHistoryChange(token);
};
