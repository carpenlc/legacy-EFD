from com.solers.delivery.scripting.event import DeliveryEventConsumer
import time

label = "blocking.py: "

class MyEventConsumer(DeliveryEventConsumer):
    def startedSupplier(self, evt):
        self.printSynchronizationEvent(evt);
    def completedSupplier(self, evt):
        self.printSynchronizationEvent(evt);
    def startedConsumer(self, evt):
        self.printSynchronizationEvent(evt);
    def completedConsumer(self, evt):
        self.printSynchronizationEvent(evt);
    def receivedContent(self, evt):
        self.printContentEvent(evt);
    def suppliedContent(self, evt):
        self.printContentEvent(evt);
    def printSynchronizationEvent(self, evt):
        print (label + " EVENT type " + evt.getClass().getName());
        print (label + " EVENT contentSetId = " + str(evt.getContentSetId()));
        print (label + " EVENT syncId = " + evt.getId());
        if evt.getResult() == None:
            print (label + "Synchronization Result = null");
        else:
            print (label + "Synchronization Result = " + evt.getResult().toString());
        print (label + "          " );
        print ("");
    def printContentEvent(self, evt):
        print (label + " EVENT type " + evt.getClass().getName());
        print (label + " EVENT contentSetId = " + str(evt.getContentSetId()));
        print (label + " EVENT syncId = " + evt.getSynchronizationId());
        print (label + "Content Action = " + evt.getAction());
        print (label + "Content Result = " + evt.getResult());
        print (label + " Content Path = " + evt.getPath() );    
        print (label + "          " );
        print ("");

listener = MyEventConsumer()
efdEventManager.addConsumer(listener)

# go into events loop
count = 1
while True:
    # blocked listening every 5 minutes
    efdEventManager.startListening(5000)
    
    #Do something here in between
    count = count + 5
    if count==300:
	print (".")
        count=0
	
# Good practice to call this when no longer need events.
# Leave it here to complete the sample. Not actually need this if the script
# exits shortly
efdEventManager.stopListening(listener)
# Do something else below ....

