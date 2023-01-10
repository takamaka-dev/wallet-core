/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.beans;

/**
 *
 * @author isacco.borsani@h2tcoin.com
 */
public class AddressBean {

    private transient String address;
    private String shortAddress;
    private boolean shortened;
    
    public AddressBean() {
    }

    public AddressBean(String address) {
        this.address = address;
        this.shortAddress = address;
        shortened = false;
    }

    public AddressBean(String address, String shortAddress, boolean shortened) {
        this.address = address;
        this.shortAddress = shortAddress;
        this.shortened = shortened;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getShortAddress() {
        return shortAddress;
    }

    public void setShortAddress(String shortAddress) {
        this.shortAddress = shortAddress;
    }

    public boolean isShortened() {
        return shortened;
    }

    public void setShortened(boolean shortened) {
        this.shortened = shortened;
    }
    
    
}
