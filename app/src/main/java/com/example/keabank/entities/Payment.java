package com.example.keabank.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class Payment implements Parcelable {

    private String payerAccountId;
    private long amount;
    private String receiverAccountId;
   // private boolean isCredit;
   // private boolean isBill;
    private int autoBillDay;

    public Payment(String payerAccountId, long amount, String receiverAccountId/*, boolean isCredit, boolean isBill*/, int autoBillDay) {
        this.payerAccountId = payerAccountId;
        this.amount = amount;
        this.receiverAccountId = receiverAccountId;
       // this.isCredit = isCredit;
      //  this.isBill = isBill;
        this.autoBillDay = autoBillDay;
    }

    protected Payment(Parcel in) {
        payerAccountId = in.readString();
        amount = in.readLong();
        receiverAccountId = in.readString();
      //  isCredit = in.readInt() != 0; // boolean will be "true" if "if statement" is not 0
      //  isBill = in.readInt() != 0;
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
        parcel.writeString(receiverAccountId);
      //  parcel.writeInt((int) (isCredit ? 1 : 0));     //if boolean == true, int == 1
      //  parcel.writeInt((int) (isBill ? 1 : 0));
        parcel.writeInt(autoBillDay);
    }


    /* GETTERS */
    public String getPayerAccountId() {
        return payerAccountId;
    }

    public long getAmount() {
        return amount;
    }

    public String getReceiverAccountId() {
        return receiverAccountId;
    }



  /*  public boolean isCredit() {
        return isCredit;
    }*/

   /* public boolean isBill() {
        return isBill;
    }*/

    public int getAutoBillDay() {
        return autoBillDay;
    }

    public void setAutoBillDay(int autoBillDay) {
        this.autoBillDay = autoBillDay;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "payerAccountId='" + payerAccountId + '\'' +
                ", amount=" + amount +
                ", receiverAccountId='" + receiverAccountId + '\'' +
                ", autoBillDay=" + autoBillDay +
                '}';
    }
}
