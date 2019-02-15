/*global EFD, Ext, System */

Ext.ns('EFD.heartbeat');

EFD.heartbeat = function() {
    
    var period = 5000;
    
    return {
        start: function() {
            this.alive = true;
            this.timer = new EFD.heartbeat.Timer();
            this.registerEvents();
            this.loop();
        }
        
        ,registerEvents: function() {
            var src = Ext.isIE ? document : window;
            var cb = this.setAlive.createDelegate(this, [true]);
            Ext.EventManager.on(src, 'click', cb);
            Ext.EventManager.on(src, 'keypress', cb);
        }
        
        ,loop: function() {
            if (this.isAlive()) {
                this.cancelTimeout();
            } else {
                this.startTimeout();
            }
            System.heartbeat();
            this.setAlive(false);
            this.loop.defer(period, this);
        }
        
        ,setAlive: function(value) {
            this.alive = value;
        }
        
        ,isAlive: function() {
            return this.alive;
        }
        
        ,startTimeout: function() {
            if (!this.timer.isStarted()) {
                this.timer.start(period);
            }
        }
        
        ,cancelTimeout: function() {
            if (this.timer.isStarted()) {
                this.timer.stop();
            }
        }
    };
}();

EFD.heartbeat.Timer = function() {
    this.warningPeriod = 30 * 1000; // 30 Seconds
    this.started = false;
};

Ext.extend(EFD.heartbeat.Timer, Object, {
    
    start: function(offset) {
        this.started = true;
        var warnTime = (EFD.util.constants.getSessionTimeout() * 1000) - (this.warningPeriod + offset);
        this.warnTimer = this.warn.defer(warnTime, this); 
    }
    
    ,stop: function() {
        if (this.warningShowing) {
            return;
        }
        this.started = false;
        if (this.warnTimer) {
           window.clearTimeout(this.warnTimer);
           delete this.warnTimer;
        }
         
        if (this.warnLoopTimer) {
           window.clearTimeout(this.warnLoopTimer);
           delete this.warnLoopTimer;
        }
    }
    
    ,isStarted: function() {
        return this.started;
    }
    
    ,warn: function() {
        var seconds = this.warningPeriod / 1000;
        this.warningShowing = true;
        
        var mb = Ext.MessageBox.show({
            title: 'Warning',
            width: 300,
            modal: true,
            closable: false,
            msg: this.formatWarning(seconds),
            buttons: Ext.MessageBox.CANCEL,
            icon: Ext.MessageBox.WARNING,
            fn: this.cancelWarning.createDelegate(this)
        });
        
        this.warnLoopTimer = this.warnLoop.defer(1000, this, [mb, seconds-1]);
    }
    
    ,cancelWarning: function() {
        this.warningShowing = false;
        this.stop();
    }
    
    ,warnLoop: function(mb, seconds) {
        if (seconds === 0) {
            mb.updateText('Logging out...');
            EFD.error.message.set('Your session has expired');
            EFD.login.logout();
            return;
        }
        
        mb.updateText(this.formatWarning(seconds));
        this.warnLoopTimer = this.warnLoop.defer(1000, this, [mb, seconds-1]);
    }
    
    ,formatWarning: function(seconds) {
        return String.format('Your session will expire in {0} {1}.', seconds, seconds == 1 ? 'second' : 'seconds');
    }
});
