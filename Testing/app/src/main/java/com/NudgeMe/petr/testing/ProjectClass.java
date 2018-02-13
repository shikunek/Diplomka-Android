package com.NudgeMe.petr.testing;

import java.util.ArrayList;

/**
 * Created by Petr on 14.10.2017.
 */

public class ProjectClass {

    private String projectName;
    private String id;
    private ArrayList<String> projectUsers = new ArrayList<>();

    public ProjectClass()
    {

    }

    public ProjectClass(String projectName, String id, ArrayList<String> projectUsers)
    {
        this.id = id;
        this.projectName = projectName;
        this.projectUsers = projectUsers;
    }

    public String getProjectName()
    {
        return  projectName;
    }
    public String getID()
    {
        return  id;
    }
    public ArrayList<String> getProjectUsers()
    {
        return  projectUsers;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addProjectUsers(String projectUsers) {
        this.projectUsers.add(projectUsers);
    }
}
