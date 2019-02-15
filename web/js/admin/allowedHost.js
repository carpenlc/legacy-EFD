/*global Ext, EFD, AllowedHostService */

Ext.ns('EFD.admin');


EFD.admin.AllowedHostForm = Ext.extend(Ext.ux.DWRForm, {
    border: false,
    frame: true,
    labelWidth: 80,
    autoScroll: true,
    
    initComponent: function() {
        Ext.apply(this, {
            layout: "form",
            saveMethod: AllowedHostService.save, 
            labelAlign: 'right',
            labelWidth: 250,
            bodyStyle:'padding:5px',
            width: 600,
            defaultType: 'textfield',
            items: [{
                fieldLabel: "Alias",
                name: "alias",
                allowBlank: false,
                itemCls: 'required',
                width: 150
            },{
                fieldLabel: "Common Name",
                name: "commonName",
                allowBlank: false,
                itemCls: 'required',
                width: 150,
                grow: true,
                growMax: 300,
                growMin: 150
            }],
            buttons: [{
                text: 'Save',
                handler: this.submit,
                scope: this
            }]
        });
        
        EFD.admin.AllowedHostForm.superclass.initComponent.apply(this, arguments);
        
        this.on('formrendered', this.formRendered, this);
        this.on('beforesave', this.beforeSave, this);
        this.on('aftersave', this.afterSave, this);
        this.on('nodedeleted', this.remove, this);
    }
    
    ,remove: function(panel, allowedHost) {
        if (this.allowedHost) {
            if (this.allowedHost.id == allowedHost.id) {
                Ext.getCmp('window-tab-panel').remove(this);
            }
        }
    }

    ,beforeSave: function(form, allowedHost) {
        if (this.allowedHost) {
            EFD.util.copyProperties(this.allowedHost, allowedHost, ['id']);
        }
    }

    ,formRendered: function(form) {
        if (this.allowedHost) {
            form.doFormLoad(this.allowedHost);
        } else {
            this.loadMask.hide();
            this.on('aftersave', this.afterNewSave, this);
        }
    }
    
    ,afterSave: function(id, allowedHost) {
        EFD.util.msg(allowedHost.alias, 'Saved');
        this.setTitle(allowedHost.alias);
    }
    
    ,afterNewSave: function(form, allowedHost, id) {
        this.allowedHost = {
            id: id
        };
        
        this.un('aftersave', this.afterNewSave, this);
    }
    
});

Ext.reg('allowedhostform', EFD.admin.AllowedHostForm);
