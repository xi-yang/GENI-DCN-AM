/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

/**
 *
 * @author root
 */
public class AggregateUser {
    int id;
    String name;
    String firstName;
    String lastName;
    String email;
    String description;

    public AggregateUser(int i, String n, String f, String l, String e, String d) {
        id = i;
        name = n;
        firstName = f;
        lastName = l;
        email = e;
        description = d;
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
