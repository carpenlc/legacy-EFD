/*global Ext, EFD */

Ext.ns("Ext.ux");

Ext.menu.Menu.method('item', function() {
    return function(name, value) {
        return this.items.find(function(i) {
            return i[name] === value;
        });
    };
}());


Ext.ux.TabCloseMenu = function() {
    var tabs;
    var menu;
    var ctxItem;
    
    this.init = function(tp) {
        tabs = tp;
        tabs.on('contextmenu', this.onContextMenu);
    };

    this.onContextMenu = function (ts, item, e) {
        ctxItem = item;
        if(!menu){ // create context menu on first right click
            menu = new Ext.menu.Menu([{
                id: tabs.id + '-close',
                text: 'Close Tab',
                handler : function(){
                    tabs.remove(ctxItem);
                }
            },{
                id: tabs.id + '-close-others',
                text: 'Close Other Tabs',
                handler : function(){
                    tabs.items.each(function(item){
                        if (item.closable && item != ctxItem){
                            tabs.remove(item);
                        }
                    });
                }
            },{
                id: tabs.id + '-close-all',
                text: 'Close All Tabs',
                handler : function(){
                    tabs.items.each(function(item){
                        if (item.closable){
                            tabs.remove(item);
                        }
                    });
                }
            }]);
        }
        
        var disableOthers = true;
        tabs.items.each(function() {
            if (this != item && this.closable){
                disableOthers = false;
                return false;
            }
        });
        menu.items.get(tabs.id + '-close-others').setDisabled(disableOthers);
        menu.items.get(tabs.id + '-close').setDisabled(!item.closable);
        
        menu.showAt(e.getPoint());
    };
};


/**
 * Extend the default AsyncTreeNode to fire an event
 * after its children are rendered
 */
Ext.ux.AsyncTreeNode = Ext.extend(Ext.tree.AsyncTreeNode, {
    renderChildren: function() {
        Ext.ux.AsyncTreeNode.superclass.renderChildren.apply(this, arguments);
        this.fireEvent('childrenrendered', this);
    }
});


/**
 * We extend the normal paging toolbar because it doesn't support
 * passing extra parameters to its Ext.data.Store.  We need to be able
 * to pass in DWR parameters
 */
Ext.ux.PagingToolBar = Ext.extend(Ext.PagingToolbar, {
    
    dwrParamFunction: Ext.emptyFn,
    startValue: 0,
    
    doLoad: function(start) {
        var o = {};
        var pn = this.paramNames;
        
        if (start < this.startValue) {
            start =  this.startValue;
        }
        
        o[pn.start] = start;
        o[pn.limit] = this.pageSize;
        
        if (this.dwrParamFunction) {
            var result = this.dwrParamFunction.call(this);
            if (Ext.isArray(result)) {
                o.dwrFunctionArgs = result;            
            }
        }
        
        this.store.load({params:o});
    }
    
});









/**
 * @class Ext.ux.DWRTreeLoader
 * @extends Ext.tree.TreeLoader
 * @author Carina Stumpf
 *
 * DWRTreeloader loads tree nodes by calling a DWR service.
 * Version 2.1
 *
 * http://carina.net/ext/examples/DWRTreeLoader/DwrTreeLoader_simple.html
 *
 */

/**
 * @constructor
 * @param cfg {Object} config A config object
 *    @cfg dwrCall the DWR function to call when loading the nodes
 */
Ext.ux.DWRTreeLoader = function(config) {
    Ext.ux.DWRTreeLoader.superclass.constructor.call(this, config);
};

Ext.extend(Ext.ux.DWRTreeLoader, Ext.tree.TreeLoader, {
    
    /**
     * Load an {@link Ext.tree.TreeNode} from the DWR service.
     * This function is called automatically when a node is expanded, but may be used to reload
     * a node (or append new children if the {@link #clearOnLoad} option is false.)
     * @param {Object} node node for which child elements should be retrieved
     * @param {Function} callback function that should be called before executing the DWR call
     */
    load : function(node, callback) {
        var cs, i, len;
        if (this.clearOnLoad) {
            while (node.firstChild) {
                node.removeChild(node.firstChild);
            }
        }
        if (node.attributes.children && node.attributes.hasChildren) { // preloaded json children
            cs = node.attributes.children;
            for (i = 0,len = cs.length; i<len; i++) {
                node.appendChild(this.createNode(cs[i]));
            }
            if (typeof callback == "function") {
                callback();
            }
        } else if (this.dwrCall) {
            this.requestData(node, callback);
        }
    }

    /**
     * Performs the actual load request
     * @param {Object} node node for which child elements should be retrieved
     * @param {Function} callback function that should be called before executing the DWR call
     */
    ,requestData : function(node, callback) {
        var callParams;
        var success, error, rootId, dataContainsRoot;

        if (this.fireEvent("beforeload", this, node, callback) !== false) {
            callParams = this.getParams(node);
    
            success = this.handleResponse.createDelegate(this, [node, callback], 1);
            error = this.handleFailure.createDelegate(this, [node, callback], 1);

            callParams.push({callback:success, exceptionHandler:error});

            this.transId = true;
            this.dwrCall.apply(this, callParams);
        } else {
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if (typeof callback == "function") {
                callback();
            }
        }
  }

    /**
     * Override this to add custom request parameters.
     */
    ,getParams : function(node) {
        return [];
    }

    /**
     * Handles a successful response.
     * @param {Object} childrenData data that was sent back by the server that contains the child nodes
     * @param {Object} parent parent node to which the child nodes will be appended
     * @param {Function} callback callback that will be performed after appending the nodes
     */
    ,handleResponse : function(childrenData, parent, callback) {
        this.transId = false;
        this.processResponse(childrenData, parent, callback);
    }

    /**
     * Handles loading error
     * @param {Object} response data that was sent back by the server that contains the child nodes
     * @param {Object} parent parent node to which child nodes will be appended
     * @param {Function} callback callback that will be performed after appending the nodes
     */
    ,handleFailure : function(response, parent, callback) {
        this.transId = false;
        this.fireEvent("loadexception", this, parent, response);
        if (typeof callback == "function") {
            callback(this, parent);
        }
        EFD.error.errorHandler(response, {});
    }

    /**
     * Process the response that was received from server
     * @param {Object} childrenData data that was sent back by the server that contains the attributes for the child nodes to be created
     * @param {Object} parent parent node to which child nodes will be appended
     * @param {Function} callback callback that will be performed after appending the nodes
     */
    ,processResponse : function(childrenData, parent, callback) {
        var i, n, nodeData;
        try {
            for (i = 0; i<childrenData.length; i++) {
                n = this.createNode(childrenData[i]);
                if (n) {
                    n.hasChildren = childrenData[i].hasChildren;
                    parent.appendChild(n);
                }
            }

            if (typeof callback == "function") {
                callback(this, parent);
            }
        } catch(e) {
            alert(e);
            this.handleFailure(childrenData);
        }
    }
});










Ext.ux.MapReader = function(){
    Ext.ux.MapReader.superclass.constructor.call(this, null, [
        {name: 'key', mapping: 'key'},
        {name: 'value', mapping: 'value'}
    ]);
};
Ext.extend(Ext.ux.MapReader, Ext.data.DataReader, {
    read : function(response) {
        var records = [];
        for (var dataItem in response) {
            var record = new this.recordType({ key: dataItem, value: response[dataItem] }, null);
            records[records.length] = record;
        }
        return {
            records : records,
            totalRecords : records.length
        };
    }
    
    ,readRecords: function(response) {
        return this.read(response);
    }
});






Ext.ux.ObjectReader = function(config, recordType){
    Ext.ux.ObjectReader.superclass.constructor.call(this, config, recordType);
};
Ext.extend(Ext.ux.ObjectReader, Ext.data.DataReader, {
    read : function(response) {
        var records = [];
        
        if (Ext.isArray(response)) {
            for (var i=0; i < response.length; i = i + 1) {
                records[records.length] = new this.recordType(response[i]);
            }
        } else {
            records[records.length] = new this.recordType(response);
        }
        return {
            records : records,
            totalRecords : records.length
        };
    }
    
    ,readRecords: function(response) {
        return this.read(response);
    }
});













Ext.ux.DWRProxy = function(config){
    Ext.apply(this, config); // necessary since the superclass doesn't call apply
    Ext.ux.DWRProxy.superclass.constructor.call(this);
};

Ext.extend(Ext.ux.DWRProxy, Ext.data.DataProxy, {

    /**
     * @cfg {Function} dwrFunction The DWR function for this proxy to call during load.
     * Must be set before calling load.
     */
    dwrFunction: null,
    
    dwrFunctionArgs: null,
    
    /**
     * @cfg {String} loadArgsKey Defines where in the params object passed to the load method
     * that this class should look for arguments to pass to the "dwrFunction".
     * The order of arguments passed to a DWR function matters.
     * Must be set before calling load.
     * See the explanation of the "params" parameter for the load function for further explanation.
     */
    loadArgsKey: 'dwrFunctionArgs',
    
    /**
     * Load data from the configured "dwrFunction",
     * read the data object into a block of Ext.data.Records using the passed {@link Ext.data.DataReader} implementation,
     * and process that block using the passed callback.
     * @param {Object} params An object containing properties which are to be used for the request to the remote server.
     * Params is an Object, but the "DWR function" needs to be called with arguments in order.
     * To ensure that one's arguments are passed to their DWR function correctly, a user must either:
     * 1. call or know that the load method was called explictly where the "params" argument's properties were added in the order expected by DWR OR
     * 2. listen to the "beforeload" event and add a property to params defined by "loadArgsKey" that is an array of the arguments to pass on to DWR.
     * If there is no property as defined by "loadArgsKey" within "params", then the whole "params" object will be used as the "loadArgs".
     * If there is a property as defined by "loadArgsKey" within "params", then this property will be used as the "loagArgs".
     * The "loadArgs" are iterated over to build up the list of arguments to pass to the "dwrFunction".
     * @param {Ext.data.DataReader} reader The Reader object which converts the data object into a block of Ext.data.Records.
     * @param {Function} callback The function into which to pass the block of Ext.data.Records.
     * The function must be passed <ul>
     * <li>The Record block object</li>
     * <li>The "arg" argument from the load function</li>
     * <li>A boolean success indicator</li>
     * </ul>
     * @param {Object} scope The scope in which to call the callback
     * @param {Object} arg An optional argument which is passed to the callback as its second parameter.
     */
    load: function(params, reader, loadCallback, scope, arg){
        var dataProxy = this;
        if (dataProxy.fireEvent("beforeload", dataProxy, params) !== false) {
            
            var loadArgs = this.dwrFunctionArgs || params[this.loadArgsKey] || params; // the Array or Object to build up the "dwrFunctionArgs"
            var dwrFunctionArgs = []; // the arguments that will be passed to the dwrFunction
            if (typeof loadArgs === 'function') {
                dwrFunctionArgs = loadArgs(params);
            } else if (loadArgs instanceof Array) {
                // Note: can't do a foreach loop over arrays because Ext added the "remove" method to Array's prototype.
                // This "remove" method gets added as an argument unless we explictly use numeric indexes.
                for (var i = 0; i < loadArgs.length; i++) {
                    dwrFunctionArgs.push(loadArgs[i]);
                }
            } else { // loadArgs should be an Object
                for (var loadArgName in loadArgs) {
                    dwrFunctionArgs.push(loadArgs[loadArgName]);
                }
            }
            
            dwrFunctionArgs.push({
                callback: function(response){
                    // call readRecords verses read because read will attempt to decode the JSON,
                    // but as this point DWR has already decoded the JSON.
                    var records = reader.readRecords(response);
                    
                    dataProxy.fireEvent("load", dataProxy, response, loadCallback);
                    loadCallback.call(scope, records, arg, true);
                },
                exceptionHandler: function(message, exception){
                    // the event is supposed to pass the response, but since DWR doesn't provide that to us, we pass the message.
                    
                    dataProxy.fireEvent("loadexception", dataProxy, message, loadCallback, exception);
                    loadCallback.call(scope, null, arg, false);
                    EFD.error.errorHandler(message, exception);
                }
            });
            
            this.dwrFunction.apply(Object, dwrFunctionArgs); // the scope for calling the dwrFunction doesn't matter, so we simply set it to Object.
        } else { // the beforeload event was vetoed
            loadCallback.call(scope || this, null, arg, false);
        }
    }
});

Ext.ux.DWRForm = Ext.extend(Ext.form.FormPanel, {
    
    initComponent: function() {
        
        this.addEvents('beforeload', 'afterload', 'beforesave', 'aftersave', 'formrendered', 'formchanged');
        
        Ext.ux.DWRForm.superclass.initComponent.apply(this, arguments);
        
        this.on('render', this.postRender, this, {delay: 1});
    }
    
    ,postRender: function() {
        this.saveMask = new Ext.LoadMask(this.getEl(), {msg: 'Saving...'});
        this.loadMask = new Ext.LoadMask(this.getEl(), {removeMask: true});
        this.loadMask.show();
        this.markRequiredLabels();
        
        var that = this;
        this.getForm().items.each(function(f) {
            f.on('change', function(field, newValue, oldValue) {
                that.fireEvent('formchanged', that, field, newValue, oldValue);
            });
        });
        
        this.fireEvent('formrendered', this);
    }
    
    ,markRequiredLabels: function() {
        var labels = Ext.DomQuery.select(".required .x-form-item-label", this.getEl().dom);
        var i;
        for (i=0; i < labels.length; i = i + 1) {
            Ext.get(labels[i]).createChild('<span class="required-marker">&nbsp;*</span>');
        }
    }
    
    ,doFormLoad: function(response) {
        this.fireEvent('beforeload', this, response);
        this.getForm().setValues(response);
        this.fireEvent('afterload', this, response);
        
        this.loadMask.hide();
    }
    
    /**
     * Submits the form. Called after Submit buttons is clicked
     * @private
     */
    ,submit:function() {
        this.saveMask.show();
        
        var obj = this.getForm().getValues();
        var args = [obj];
        
        this.fireEvent('beforesave', this, obj, args);
        
        args.push({
            callback: this.onSuccess.createDelegate(this, [obj], 1),
            postHook: this.hideMask.createDelegate(this)
        });
        
        this.saveMethod.apply(this, args);
    } // eo function submit
    
    ,onSuccess: function(id, obj) {
        this.saveMask.hide();
        this.fireEvent('aftersave', this, obj, id);
    } // eo function onSuccess
    
    
    ,hideMask: function() {
        this.saveMask.hide();
        return true; // True to let another exception handler process the exception
    }
});

Ext.ux.HelpField = Ext.extend(Ext.form.TriggerField, {
    
    triggerClass: 'form-help-trigger',
    windowWidth: 500,
    windowHeight: 400,
    title: 'Help',
    
    onTriggerClick: function() {
        if (!this.win) {
            this.win = this.createWindow();
        }
        this.win.show(this.el);
    }
    
    ,createWindow: function() {
        return new Ext.Window({
            modal: true,
            closable: true,
            title: this.title,
            width: this.windowWidth,
            height: this.windowHeight,
            autoScroll: true,
            layout: 'fit',
            closeAction: 'hide',
            contentEl: this.contentEl
        });
    }
});

Ext.reg('dwrform', Ext.ux.DWRForm);
Ext.reg('helpfield', Ext.ux.HelpField);
