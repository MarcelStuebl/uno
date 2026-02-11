package htl.steyr.uno.server.serverconnection;

public interface PublisherInterface {

    void addSubscriber(SubscriberInterface subscriber);
    void notifySubscribers(Object message);


}




