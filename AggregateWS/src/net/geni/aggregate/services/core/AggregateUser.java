/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;


/**
 *
 * @author root
 */
public class AggregateUser extends AggregateResource {
    int id = 0;
    String name = "";
    String firstName = "";
    String lastName = "";
    String email = "";
    String description = "";

    public AggregateUser() { }

    public AggregateUser(int i, String n, String f, String l, String e, String d) {
        id = i;
        name = n;
        firstName = f;
        lastName = l;
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
}
