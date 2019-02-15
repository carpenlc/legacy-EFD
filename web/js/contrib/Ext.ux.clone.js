/**
 * From: http://extjs.com/forum/showthread.php?t=26644
 * the latest revision (with a better isArray check) from the author's original implementation
 * at: http://blog.extjs.eu/know-how/array-overrides/
 * 
 * On a related note, ExtJS's implementation as of 2.2.1 (Ext.isArray) can have issues as described here: 
 * http://thinkweb2.com/projects/prototype/instanceof-considered-harmful-or-how-to-write-a-robust-isarray/
 * because the implementation is:
 * isArray:function(v){
 *     return v && typeof v.length == "number" && typeof v.splice == "function";
 * }
 * When ExtJS's implementation is updated or overridden, it can be used here instead of:
 * '[object Array]' === Object.prototype.toString.call(o) ? [] : {}
 */

/*global Ext*/

/**
 * Clone Function
 * @return {Object/Array} Deep clone of an object or an array
 */
Ext.ux.clone = function(o) {
    if(!o || 'object' !== typeof o) {
        return o;
    }
    var c = '[object Array]' === Object.prototype.toString.call(o) ? [] : {};
    var p, v;
    for(p in o) {
        if(o.hasOwnProperty(p)) {
            v = o[p];
            if(v && 'object' === typeof v) {
                c[p] = Ext.ux.clone(v);
            }
            else {
                c[p] = v;
            }
        }
    }
    return c;
}; // eo function clone
