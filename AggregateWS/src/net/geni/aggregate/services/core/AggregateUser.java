/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.Serializable;

/**
 *
 * @author root
 */
public class AggregateUser implements java.io.Serializable {
    int id = 0;
    String name = "";
    String password = "";
    String role = "";
    String certSubject = "";
    String firstName = "";
    String lastName = "";
    String email = "";
    String description = "";

    public AggregateUser() { }

    public AggregateUser(int i, String n, String f, String l, String r, String s, String e, String d) {
        id = i;
        name = n;
        firstName = f;
        lastName = l;
        role = r;
        certSubject = s;
        email = e;
        description = d;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    public String getCertSubject() {
        return certSubject;
    }

    public void setCertSubject(String certSubject) {
        this.certSubject = certSubject;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
