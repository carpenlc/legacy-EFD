load("nashorn:mozilla_compat.js");
var DeliveryEventConsumer = JavaImporter(com.solers.delivery.scripting.event.DeliveryEventConsumer);
var label = "blocking.js: ";
var object = {
		startedSupplier: function(evt) {
			this.printSynchronizationEvent(evt);
		},
		
		completedSupplier: function(evt) {
			this.printSynchronizationEvent(evt);
		},
		
		startedConsumer: function(evt) {
			this.printSynchronizationEvent(evt);
		},
		
		completedConsumer: function(evt) {
			this.printSynchronizationEvent(evt);
		},
		
		suppliedContent: function(evt) {
			this.printContentEvent(evt);
		},
		
		receivedContent: function(evt) {
			this.printContentEvent(evt);
		},
		
		printSynchronizationEvent: function(evt) {
			 println(label + " EVENT type " + evt.getClass().getName());
			 println(label + " EVENT contentSetId = " + evt.getContentSetId() );
			 println(label + " EVENT syncId = " + evt.getId() );
			 println(label + " Synchronization Result = " + evt.getResult() );
			 println("");
		},
		
		printContentEvent: function(evt) {
			 println(label + " EVENT type " + evt.getClass().getName());
			 println(label + " EVENT contentSetId = " + evt.getContentSetId() );
			 println(label + " EVENT syncId = " + evt.getSynchronizationId() );
			 println(label + " Content Action = " + evt.getAction() );
			 println(label + " Content Result = " + evt.getResult() );	 
			 println(label + " Content Path = " + evt.getPath());
			 println("");
		}
};

//create java equivalent object
var listener = new JavaAdapter(DeliveryEventConsumer, object);
//add as consumer
efdEventManager.addConsumer(listener);
// go into events loop
var count = 1;
while( true ) {
    // blocked listening
    efdEventManager.startListening(5000);

    // Do something here in between    
    count = (count + 1)%60;
	if(count === 0) {
		println(".");
	}

}
// good practice to call this when no longer need events.
// Leave it here to complete the sample. Not actually need this if the script
// exits shortly
efdEventManager.stopListening(listener);
// Do something else below ....

