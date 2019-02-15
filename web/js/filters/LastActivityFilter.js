/*global Ext */

Ext.grid.filter.LastActivityFilter = Ext.extend(Ext.grid.filter.ListFilter, {
    single: true,
    lastActivityKey: 'lastUpdateInterval',
    
    validateRecord: function(record) {
        var lastActivity = record.get(this.lastActivityKey);
        var valueInMilliseconds = this.getValue()[0] * 1000 * 60;
        return lastActivity !== 0 && lastActivity < valueInMilliseconds;
    }
    
    //Expected option format: [{id: 5, text: '> 5m'}, ..., {id:60, text:'> 60m'}]
    //where id is value[0] and a numeric representation of a time value in minutes
});
