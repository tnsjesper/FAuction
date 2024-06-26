package fr.florianpal.fauction.languages;


import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

public enum MessageKeys implements MessageKeyProvider {
    NO_AUCTION,
    AUCTION_OPEN,
    AUCTION_ADD_SUCCESS,
    REMOVE_AUCTION_SUCCESS,
    BUY_YOUR_ITEM,
    ITEM_AIR,
    NO_HAVE_MONEY,
    BUY_AUCTION_SUCCESS,
    AUCTION_EXPIRE,
    AUCTION_ALREADY_SELL,
    NEGATIVE_PRICE,
    MAX_AUCTION,
    AUCTION_EXPIRE_DROP,
    BUY_AUCTION_CANCELLED,
    MIN_PRICE,

    SPAM,

    AUCTION_RELOAD,

    REMOVE_EXPIRE_SUCCESS,

    TRANSFERT_BDD,

    MAX_BILL,
    BILL_ADD_SUCCESS,
    NO_BILL,

    BILL_ALREADY_SELL,

    MAKE_OFFER_BILL_SUCCESS,

    BUY_BILL_CANCELLED,

    REMOVE_BILL_SUCCESS,

    AMOUNT_LESS_THAN_BET,

    DATABASEERROR;

    private static final String PREFIX = "fauction";

    private final MessageKey key = MessageKey.of(PREFIX + "." + this.name().toLowerCase());

    public MessageKey getMessageKey() {
        return key;
    }
}
