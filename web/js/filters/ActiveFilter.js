/*global Ext */

Ext.grid.filter.ActiveFilter = Ext.extend(Ext.grid.filter.ListFilter, {
    
    activeValueKey: 'active',
    resultValueKey: 'result',
    modifiedResultParam: 'modified',
    warningResultParam: 'warning',
    failureResultParam: 'failure',
    emptyResultParam: 'empty',
    
    validateRecord: function(record) {
        var valid = false, active = record.get(this.activeValueKey);
        if (active) {
            valid = this.checkForValue(this.activeValueKey);
        } else {
            var result = record.get(this.resultValueKey);
            if (result.modified) {
                valid = this.checkForValue(this.modifiedResultParam);
            } else if (result.warning) {
                valid = this.checkForValue(this.warningResultParam);
            } else if (result.failure) {
                valid = this.checkForValue(this.failureResultParam);
            } else {
                valid = this.checkForValue(this.emptyResultParam);
            }
        }
        return valid;
    }
    
    ,optionIsInMenu: function(option) {
        //Check if an option is in the menu at all 
        //(e.g. if configured with options: ['modified', 'warning'], 'empty', and 'failure'
        // aren't in the menu. This function would allow an extension of this filter
        // to handle that case.)
        var optionIsInMenu = false;
        this.menu.items.each(function(item) {
            if (item.itemId === option) {
                optionIsInMenu = true;
                return false;
            }
        });
        return optionIsInMenu;
    }
    
    ,checkForValue: function(value) {        
        //If the value is not selected in the menu 
        //(even if it is excluded from the menu altogether - i.e. !this.optionIsInMenu(value)),
        //it should be filtered out.
        return this.getValue().indexOf(value) > -1;   
    }
});
