/*global Ext, EFD, dwr, PasswordExpired, LoginRequired, AccessDenied */

Ext.ns('EFD.error');

EFD.error.message = function() {
    
    var COOKIE_NAME = "efd-current-message";
    
    var createCookie = function (name,value,ms) {
        var expires = "";
        if (ms) {
            var date = new Date();
            date.setTime(date.getTime()+ms);
            expires = "; expires="+date.toGMTString();
        }
        document.cookie = name+"="+value+expires+"; path=/; secure";
    };
    
    var readCookie = function (name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        var i;
        for(i=0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') {
                c = c.substring(1, c.length);
            }
            if (c.indexOf(nameEQ) === 0) {
                return c.substring(nameEQ.length, c.length);
            }
        }
        return null;
    };
    
    var eraseCookie = function (name) {
        createCookie(name,"",-1);
    };
    
    return {
        
        get: function() {
            return readCookie(COOKIE_NAME);
        }
        
        ,set: function(msg) {
            this.clear();
            createCookie(COOKIE_NAME, msg, 1000*10);
        }
        
        ,clear: function() {
            eraseCookie(COOKIE_NAME);
        }
        
        ,isSet: function() {
            return this.get() !== null;
        }
        
    };
    
}();

EFD.error.errorHandler = function() {
    
    var tpl = new Ext.XTemplate('<ul class="error"><tpl for="msgs"><li>{.}</li></tpl></ul>');
    
    return function(msg, ex) {    
        var msgs = ex.messages || [msg];
        
        // For debugging.
        if ((msg === 'Error' || msg === '') && ex.javaClassName) {
            msgs.push(ex.javaClassName);    
        }
                    
        Ext.MessageBox.show({
            title: 'Error',
            width: 300,
            msg: tpl.apply({msgs: msgs}),
            buttons: Ext.MessageBox.OK,
            icon: Ext.MessageBox.ERROR
        });
    };
}();

EFD.error.toIndexHandler = function(msg, ex) {
    if (!EFD.error.message.isSet()) {
        EFD.error.message.set(msg);
    }
    window.location = '/';
};

EFD.error.passwordExpiredHandler = function(msg, ex) {
    var win = new EFD.login.ChangePasswordWindow({logout: true});
    win.show(); 
};

EFD.error.defaultHandler = function(msg, ex) {
    if (ex instanceof PasswordExpired) {
        EFD.error.passwordExpiredHandler(msg, ex);
    } else if (ex instanceof LoginRequired) {
        EFD.error.toIndexHandler(msg, ex);
    } else if (ex instanceof AccessDenied) {
        EFD.error.toIndexHandler(msg, ex);
    } else {
        EFD.error.errorHandler(msg, ex);
    }
};

Ext.onReady(function() { 
    dwr.engine.setErrorHandler(EFD.error.defaultHandler);
});
