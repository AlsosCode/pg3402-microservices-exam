package no.kristiania.pg3402.collection.exception;

public class CardAlreadyOwnedException extends RuntimeException {
    public CardAlreadyOwnedException(String message) {
        super(message);
    }
}
