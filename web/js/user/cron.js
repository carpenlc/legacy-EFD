/*global Ext, EFD */

Ext.ns('EFD.cron');

EFD.cron.formatDaily = function(timeValue, days) {
    var timeParts = timeValue.split(':');
    var hour = parseInt(timeParts[0], 10);
    var min = parseInt(timeParts[1], 10);
    
    return '59 '+min+' '+hour+' ? * '+days+' *';
};


EFD.cron.parseDaily = function(expr) {
    // Expr format:
    // SEC MIN HOUR DAYOFMONTH MONTH DAYSWEEK YEAR
    var parts = expr.split(' ');
    var min;
    var hour;
    var days;
    var checked = [];
    var part;
    var i;
    var timeValue = null;
    
    // Remove seconds
    parts.shift();
    
    min = parts.shift().trim();
    hour = parts.shift().trim();
    
    if (min !== '*' && hour !== '*') {
        timeValue = String.leftPad(hour, 2, '0')+String.leftPad(min, 2, '0');
    }
    
    // Remove Day of Month and Month
    parts.shift();
    parts.shift();
    
    days = parts.shift().trim();
    if (days.indexOf(',') === -1) {
        days = parseInt(days, 10);
        if (days >= 1 && days <= 7) {
            checked.push(days);
        }
    } else {
        days = days.split(',');
        for (i=0; i < days.length; i = i + 1) {
            part = days[i].trim();
            if (part !== '*') {
                part = parseInt(part, 10);
                if (part >=1 && part <= 7) {
                    checked.push(part);
                }
            }
        }
    }
    
    return {
        timeValue: timeValue,
        days: checked
    };
};

EFD.cron.isDailyExpression = function() {

    var regex = /^59\s*\d{1,2}\s*\d{1,2}\s*\?\s*\*\s*(((\d,)+\d)|\d)\s*\*$/;
    
    return function(expr) {
        return regex.test(expr.trim());    
    };
}();
