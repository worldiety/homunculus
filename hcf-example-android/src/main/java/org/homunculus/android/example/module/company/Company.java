package org.homunculus.android.example.module.company;

import javax.persistence.*;

@Entity(name = "company")
public class Company {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "title_name")
    private String titleName;
    @Basic(optional = false)
    @Column(name = "title_color")
    private String titleColor;

    public Company() {
    }

    public Company(String id) {
        this.id = id;
    }

    public Company(String id, String titleName, String titleColor) {
        this.id = id;
        this.titleName = titleName;
        this.titleColor = titleColor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id='" + id + '\'' +
                ", titleName='" + titleName + '\'' +
                ", titleColor='" + titleColor + '\'' +
                '}';
    }
}
