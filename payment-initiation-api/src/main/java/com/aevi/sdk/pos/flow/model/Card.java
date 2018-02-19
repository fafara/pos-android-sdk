package com.aevi.sdk.pos.flow.model;

import com.aevi.sdk.flow.model.AdditionalData;
import com.aevi.sdk.flow.model.Token;
import com.aevi.util.json.JsonConverter;
import com.aevi.util.json.Jsonable;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * The card details representing the card presented during a transaction.
 *
 * All fields in this class are optional and the payment service used may or may not return all these values.
 */
public class Card implements Jsonable {

    private static final int MAX_PAN_DIGITS_ALLOWED = 10;

    private final String maskedPan;
    private final String cardholderName;
    private final String expiryDate;
    private final Token cardToken;
    private final AdditionalData additionalData;

    public static Card getEmptyCard() {
        return new Card(null, null, null, null, null);
    }

    Card(String maskedPan, String cardholderName, String expiryDate, Token token, AdditionalData additionalData) {
        if (maskedPan != null && maskedPan.replaceAll("\\D", "").length() > MAX_PAN_DIGITS_ALLOWED) {
            throw new IllegalArgumentException("Masked PAN must not contain more than " + MAX_PAN_DIGITS_ALLOWED + " non-masked digits");
        }
        this.maskedPan = maskedPan;
        this.cardholderName = cardholderName;
        this.expiryDate = expiryDate;
        this.cardToken = token;
        this.additionalData = additionalData != null ? additionalData : new AdditionalData();
    }

    /**
     * Get the masked PAN of the card presented.
     *
     * Can be null.
     *
     * @return The masked PAN of the card presented
     */
    @Nullable
    public String getMaskedPan() {
        return maskedPan;
    }

    /**
     * Get the name of the card holder.
     *
     * Can be null.
     *
     * @return The name of the cardholder
     */
    @Nullable
    public String getCardholderName() {
        return cardholderName;
    }

    /**
     * Get the expiry date of the card presented in the format YYYYMMDD.
     *
     * Can be null.
     *
     * @return The expiry date of the card presented in the format YYYYMMDD
     */
    @Nullable
    public String getExpiryDate() {
        return expiryDate;
    }

    /**
     * Get the card token genereated for the presented card.
     *
     * Can be null.
     *
     * @return The tokenised value for the card used if one was generated by the payment app
     */
    @Nullable
    public Token getCardToken() {
        return cardToken;
    }

    /**
     * Get any additional data available for this card.
     *
     * @return An {@link AdditionalData} object with additional data
     */
    @NonNull
    public AdditionalData getAdditionalData() {
        return additionalData;
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    @Override
    public String toString() {
        return "Card{" +
                "maskedPan='" + maskedPan + '\'' +
                ", cardholderName='" + cardholderName + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", cardToken=" + cardToken +
                ", additionalData=" + additionalData +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (maskedPan != null ? !maskedPan.equals(card.maskedPan) : card.maskedPan != null) return false;
        if (cardholderName != null ? !cardholderName.equals(card.cardholderName) : card.cardholderName != null) return false;
        if (expiryDate != null ? !expiryDate.equals(card.expiryDate) : card.expiryDate != null) return false;
        if (cardToken != null ? !cardToken.equals(card.cardToken) : card.cardToken != null) return false;
        return additionalData != null ? additionalData.equals(card.additionalData) : card.additionalData == null;
    }

    @Override
    public int hashCode() {
        int result = maskedPan != null ? maskedPan.hashCode() : 0;
        result = 31 * result + (cardholderName != null ? cardholderName.hashCode() : 0);
        result = 31 * result + (expiryDate != null ? expiryDate.hashCode() : 0);
        result = 31 * result + (cardToken != null ? cardToken.hashCode() : 0);
        result = 31 * result + (additionalData != null ? additionalData.hashCode() : 0);
        return result;
    }

}
