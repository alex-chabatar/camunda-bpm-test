package org.camunda.bpm.extension.test.process.model;

import java.io.Serializable;
import java.util.Date;

public class UserTaskProperties implements Serializable {

  private String name;
  private String assignee;
  private String kandidat;
  private String rolle;
  private Integer prio;
  private Date dueDate;
  private Date followUpDate;
  private String description;
  private String comment;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getKandidat() {
    return kandidat;
  }

  public void setKandidat(String kandidat) {
    this.kandidat = kandidat;
  }

  public String getRolle() {
    return rolle;
  }

  public void setRolle(String rolle) {
    this.rolle = rolle;
  }

  public Integer getPrio() {
    return prio;
  }

  public void setPrio(Integer prio) {
    this.prio = prio;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public Date getFollowUpDate() {
    return followUpDate;
  }

  public void setFollowUpDate(Date followUpDate) {
    this.followUpDate = followUpDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

}