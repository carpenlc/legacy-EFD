/*global Ext, EFD */

Ext.ns('EFD');

EFD.current = function() {
	//for storing user object
	//create extra stores to support other functions
	var userStore = {
		user: null
	};
	
	var model = {
		//begin methods for supporting userStore
		getUser: function() {
			return userStore.user;
		}
		,setUser: function(user) {
			userStore.user = user;
		}
		//end methods for supporting userStore
		
		//add new methods here to support new stores
	};
	
	return model;
}();
