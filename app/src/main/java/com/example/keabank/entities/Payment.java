package com.example.keabank.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class Payment implements Parcelable {

    private String payerAccountId;
    private long amount;
    private String receiverAccountOrBillId;
    private int autoBillDay;

    public Payment(String payerAccountId, long amount, String receiverAccountOrBillId/*, boolean isCredit, boolean isBill*/, int autoBillDay) {
        this.payerAccountId = payerAccountId;
        this.amount = amount;
        this.receiverAccountOrBillId = receiverAccountOrBillId;
        this.autoBillDay = autoBillDay;
    }

    protected Payment(Parcel in) {
        payerAccountId = in.readString();
        amount = in.readLong();
        receiverAccountOrBillId = in.readString();
        autoBillDay = in.readInt();
    }

    public static final Creator<Payment> CREATOR = new Creator<Payment>() {
        @Override
        public Payment createFromParcel(Parcel in) {
            return new Payment(in);
        }

        @Override
        public Payment[] newArray(int size) {
            return new Payment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(payerAccountId);
        parcel.writeLong(amount);
        parcel.writeString(receiverAccountOrBillId);
        parcel.writeInt(autoBillDay);
    }

    /* GETTERS */
    public String getPayerAccountId() {
        return payerAccountId;
    }

    public long getAmount() {
        return amount;
    }

    public String getReceiverAccountOrBillId() {
        return receiverAccountOrBillId;
    }

    public int getAutoBillDay() {
        return autoBillDay;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "payerAccountId='" + payerAccountId + '\'' +
                ", amount=" + amount +
                ", receiverAccountOrBillId='" + receiverAccountOrBillId + '\'' +
                ", autoBillDay=" + autoBillDay +
                '}';
    }
}
