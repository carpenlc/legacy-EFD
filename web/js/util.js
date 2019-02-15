/*global Ext, EFD */

Function.prototype.method = function (name, func) {
    this.prototype[name] = func;
    return this;
};

/**
 * Adds a toDTG() method to all Date objects.
 */
Date.method('toDTG', function() {
    
    var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    
    var pad = function (value) {
        var s = "" + value;
        if (s.length === 1) {
            return "0" + value;
        } else {
            return value;
        }
    };
    
    return function() {
        var formatted = "";
        formatted += pad(this.getDate());
        formatted += pad(this.getHours());
        formatted += pad(this.getMinutes());
        formatted += pad(this.getSeconds());
        formatted += "Z ";
        formatted += months[this.getMonth()];
        formatted += " ";
        formatted += this.getFullYear();
        return formatted;
    };
}());

/**
 * Add a limit method to all Strings that will shorten
 * the string to the given length
 */
String.method('limit', function() {

    return function(limit) {
        var result = this;
        var len = this.length;
        if (len > limit) {
            result = this.slice(0, limit)+"...";
        }
        return result;
    };
    
}());

String.method('blank', function() {
    
    var whitespace = function(c) {
        return c === ' ' || c === '\n' || c === '\r';
    };
    
    return function() {
        var i;
        var len = this.length;
        for (i=0; i < len; i=i+1) {
            if (!whitespace(this[i])) {
                return false;
            }
        }
        return true;
    };
    
}());


Array.method('each', function() {
    
    return function(func) {
        var i;
        var len = this.length;
        for (i=0; i < len; i = i + 1) {
            func(this[i]);
        }
    };
    
}());

Ext.applyIf(Array.prototype, {
    //Firefox has a built-in map function
    //From: https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/map#Compatibility
    map : function(fun /*, thisp*/)
    {
        var len = this.length >>> 0;
        if (typeof fun != "function") {
          throw new TypeError();
        }
        var res = [];
        [].length = len;
        var thisp = arguments[1];
        for (var i = 0; i < len; i++)
        {
          if (i in this) {
            res[i] = fun.call(thisp, this[i], i, this);
          }
        }
        
        return res;
    }
});

Ext.apply(Ext.form.VTypes, {
    
    password: function(val, field) {
        if (field.initialPassField) {
            var pwd = Ext.getCmp(field.initialPassField);
            return (val === pwd.getValue());
        }
        return true;
    },
    
    passwordText: 'Passwords do not match'
});


Ext.ns('EFD.util');


EFD.util.constants = function() {
    
    var config = {};
    
    return {
        getPageSize: function() {
            return config.pageSize;
        }
        
        ,isGbsEnabled: function() {
            return config.gbsEnabled;
        }
        
        ,getSessionTimeout: function() {
            return config.timeout;
        }
        
        ,getContentEventActions: function() {
            return config.contentEventActions;
        }
        
        ,getFilterTypes: function() {
            return config.filterTypes;
        }
        
        ,getFileSizeUnits: function() {
            return config.fileSizeUnits;
        }
        
        ,getTimeIntervals: function() {
            return config.timeIntervals;
        }
        
        ,init: function(data) {
            config = data;
        }
    };
    
}();


/**
 * Format a byte value into a human readable format
 */
EFD.util.formatBytes = function() {
    
    var units = ["B", "KB", "MB", "GB", "TB"];
    var threshold = 1024;
    var precision = 2;
    
    return function formatBytes(value, unit) {
        unit = unit || 0;
        if (value >= threshold) {
            return formatBytes(value / threshold, unit + 1);
        }
        if (unit === 0) {
            return "" + Math.round(value) + " " + units[unit];
        }
        return "" + value.toFixed(precision) + " " + units[unit];
    };
    
}();

/**
 * Format a number of milliseconds representing a time interval
 * into a human readable format
 */ 
EFD.util.formatTime = function() {
    
    var time = {
        label: ["s", "m", "h", "d", "w"],
        units: [1000, 60000, 3600000, 86400000, 604800000] 
    };
    
    return function(value) {
        if (!value || value < 1000) {
            return "0s";
        }
        
        var result = "";
        for (var i = time.units.length - 1; i >= 0; i--) {
            var size = time.units[i];
            if (value >= size) {
                result += Math.floor(value / size) + time.label[i] + " ";
                value -= Math.floor(value / size) * size;
            }
        }
        return result.slice(0, result.length - 1);
    };
    
}();

/**
 * Show an info message at the top of the screen
 */
EFD.util.msg = function() {
    
    var msgCt;
    
    var createBox = function(t, s) {
        return ['<div class="msg">',
            '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
            '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc"><h3>', t, '</h3>', s, '</div></div></div>',
            '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
            '</div>'].join('');
    };
    
    return function(title, format) {
        if (!msgCt) {
            msgCt = Ext.DomHelper.insertFirst(document.body, {id:'msg-div'}, true);
        }
        msgCt.alignTo(document, 't-t');
        var s = String.format.apply(String, Array.prototype.slice.call(arguments, 1));
        var m = Ext.DomHelper.append(msgCt, {html: createBox(title, s)}, true);
        m.slideIn('t').pause(1).ghost("t", {remove: true});
    };
}();

EFD.util.ask = function(title, msg, callback, config) {
    
    if (!config || typeof config === 'object') {
      config = {};
    }
    Ext.apply(config,{
        title: title,
        width: 300,
        modal: true,
        msg: msg,
        buttons: Ext.MessageBox.OKCANCEL,
        icon: Ext.MessageBox.QUESTION,
        fn: function(buttonId) { if (buttonId === 'ok') {callback.call();}}
    });
    Ext.MessageBox.show(config);
    
};

EFD.util.copyProperties = function(src, dst, props) {
    var i;
    var p;
    var len = props.length;
    for (i=0; i < len; i = i + 1) {
        p = props[i];
        if (src.hasOwnProperty(p)) {
            dst[p] = src[p];
        }
    }
};


Ext.ns('EFD.util.renderers');

EFD.util.renderers = function() {
    
    return {
        DATE: function(date) { return typeof date === 'number' ? new Date(date).toDTG() : date.toDTG(); },
        FILE_SIZE: function(value) { return EFD.util.formatBytes(value, 0); },
        TIME_INTERVAL: function(value) { return EFD.util.formatTime(value); }
    };
    
}();
