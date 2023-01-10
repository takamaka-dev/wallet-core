/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

/**
 *
 * @author giovanni.antino@h2tcoin.com
 */
public class ExtendedTransactionBox extends TransactionBox implements Comparable<TransactionBox> {

    private boolean etboxValid;
    private AddressBean shortFrom;
    private AddressBean shortTo;
    private boolean shrinkedFrom;
    private boolean shrinkedTo;
    
    public boolean isShrinkedFrom() {
        return shrinkedFrom;
    }

    public void setShrinkedFrom(boolean shrinkedFrom) {
        this.shrinkedFrom = shrinkedFrom;
    }

    public boolean isShrinkedTo() {
        return shrinkedTo;
    }

    public void setShrinkedTo(boolean shrinkedTo) {
        this.shrinkedTo = shrinkedTo;
    }

    public ExtendedTransactionBox() {
    }

    /**
     *
     * @return from if the address is not shrinked, shrinked form if is shrinked
     * and null if null
     */
    public String dbFrom() {
        if (isShrinkedFrom()) {
            return getShortFrom().getShortAddress();
        } else {
            return this.from();
        }
    }

    /**
     *
     * @return from if the address is not shrinked, shrinked form if is shrinked
     * and null if null
     */
    public String dbTo() {
        if (isShrinkedTo()) {
            return getShortTo().getShortAddress();
        } else {
            return this.to();
        }
    }

    public AddressBean getShortFrom() {
        return shortFrom;
    }

    public void setShortFrom(AddressBean shortFrom) {
        this.shortFrom = shortFrom;
    }

    public AddressBean getShortTo() {
        return shortTo;
    }

    public void setShortTo(AddressBean shortTo) {
        this.shortTo = shortTo;
    }

    /**
     *
     * @return returns the value of the flag relative to the ETBox creation
     * procedure. To verify the validity of the object use the isValid() method
     */
    public boolean isEtboxValid() {
        return etboxValid;
    }

    public void setEtboxValid(boolean etboxValid) {
        this.etboxValid = etboxValid;
    }
    
    /**
     *
     * @return returns the global validity of the object
     */
    @Override
    public boolean isValid() {
        return super.isValid() & etboxValid;
    }
}
