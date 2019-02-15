/*global Ext, EFD */

Ext.ns('EFD.preferences');

/*
 * Expected units are defined in TimeIntervalUnit.java  
 *  'S' for seconds
 *  'M' for minutes
 *  'H' for hours
 *  'D' for days
 *  'MS' for milliseconds
 */
EFD.preferences.Properties = function() {
	var properties = {
	        preferencesCookieKey: 'dashboardPreferences',
	        dashboardPreferencesUpdateEventName: 'dashboardPreferencesUpdate',
	        defaults: {
	            refresh: {
	                interval: 3,
	                unit: 'S',
	                intervalMilliseconds: 3000
	            }
	            ,ageOut: {
	                interval: 30,
	                unit: 'M',
	                intervalMilliseconds: 30 * 60 * 1000 //30 minutes
	            }
	        },
	        //30 days
	        cookieTimeOffset: 1000 * 60 * 60 * 24 * 30, //30 days
	        intervalConversionMap: {
	            D: (60 * 1000 * 60 * 24),
	            H: (60 * 1000 * 60),
	            M: (60 * 1000),
	            S: 1000,
	            MS: 1   
	        },
            maximumRecentEntries: 5000
	};
	
	return properties;
}();

EFD.preferences.PreferenceManager = function() {
 
    var PM = {
        getCookieProvider: function() {
            var cp = new Ext.state.CookieProvider({
                path: '/',
                expires: new Date(new Date().getTime() + EFD.preferences.Properties.cookieTimeOffset ),
                secure: true
            });
            return cp;
        }
        
        ,getPreferencesDirect: function() {
            var prefs = PM.getCookieProvider().get(EFD.preferences.Properties.preferencesCookieKey, EFD.preferences.Properties.defaults);

            //Guarantee that the browser doesn't have an old or invalid cookie
            //If an interval value is null or undefined then timeouts and deferred
            //functions will run as if their delay is 0, potentially pegging
            //the cpu
            prefs = this.normalize(prefs, EFD.preferences.Properties.defaults);
            
            //intervalMilliseconds for both refresh and age out should be calculated
            //from the interval and unit
            prefs = this.initializeTransientFields(prefs);

            return prefs;
        }

        ,setPreferencesDirect: function(config){
            var prefs = this.normalize(config, EFD.preferences.Properties.defaults);
            
            prefs = this.initializeTransientFields(prefs);
            
            PM.getCookieProvider().set(EFD.preferences.Properties.preferencesCookieKey, prefs);
            
            return prefs;
        }
        
        ,initializeTransientFields: function(prefs) {
            [prefs.refresh,prefs.ageOut].each(function(pref){
                Ext.apply(pref, {
                    intervalMilliseconds: pref.interval * EFD.preferences.Properties.intervalConversionMap[pref.unit]
                });
            });
            return prefs;
        }
        
        // Guarantee that an object has the required fields 
        // and that those fields are the correct type
        ,normalize: function(object, defaultObject) {
            var normalizedObject = {};
            if (typeof object === 'object') {
                //Copy values first from the given object and then from defaults
                //Preserve references from object, but clone the default
                //object so that its references aren't passed
                Ext.apply(normalizedObject, object, Ext.ux.clone(defaultObject));

                for( var field in defaultObject ) {                    
                    if (typeof normalizedObject[field] !== typeof defaultObject[field]) {
                        //If the given object has a field of the wrong type, overwrite it
                        //with a copy of the same field on the default object
                        normalizedObject[field] = Ext.ux.clone(defaultObject[field]);
                    } else if (typeof object[field] === 'object'){
                        normalizedObject[field] = this.normalize(normalizedObject[field], defaultObject[field]);
                    }
                }
            } else {
                //If the object isn't an object, copy the defaults into the normalizedObject
                Ext.apply(normalizedObject, Ext.ux.clone(defaultObject));
            }
            
            return normalizedObject;
        }
    };

    PM.prefManager = Ext.apply(new Ext.util.Observable(), {
        getPreferences: function(config) {
            config.callback(PM.getPreferencesDirect());
        }

        ,setPreferences: function(config) {
            var old = PM.getPreferencesDirect(),
                refreshIntervalUpdate = old.refresh.interval !== config.refresh.interval || old.refresh.unit !== config.refresh.unit,
                ageOutUpdate = old.ageOut.interval !== config.ageOut.interval || old.ageOut.unit !== config.ageOut.unit;
               
            if (refreshIntervalUpdate || ageOutUpdate) {
                this.fireEvent(EFD.preferences.Properties.dashboardPreferencesUpdateEventName, PM.setPreferencesDirect(config));
            }
        }
    });
    PM.prefManager.addEvents(EFD.preferences.Properties.dashboardPreferencesUpdateEventName);

    return PM.prefManager;
}();
