/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jira.reporting.plugin.web;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.UserReportingSettingsHelper;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * Admin settings page.
 */
public class ReportingUserSettingsWebAction extends JiraWebActionSupport {

  private static final int DEFAULT_PAGE_SIZE = 20;

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The first day of the week.
   */
  private String contextPath;

  private String issueCollectorSrc;

  /**
   * The message.
   */
  private String message = "";

  private int pageSize;

  private PluginCondition pluginCondition;

  private ReportingCondition reportingCondition;

  private PluginSettingsFactory settingsFactory;

  private boolean userPopupVisible;

  private boolean worklogTimeInSeconds;

  /**
   * ReportingUserSettingsWebAction constructor.
   *
   * @param settingsFactory
   *          Jira plugin settings factory.
   * @param reportingPlugin
   *          Reporting plugin.
   * @param timetrackerPlugin
   *          Timetarcker plugin.
   */
  public ReportingUserSettingsWebAction(final PluginSettingsFactory settingsFactory,
      final ReportingPlugin reportingPlugin, final JiraTimetrackerPlugin timetrackerPlugin) {
    this.settingsFactory = settingsFactory;
    reportingCondition = new ReportingCondition(reportingPlugin);
    pluginCondition = new PluginCondition(timetrackerPlugin);
  }

  private String checkConditions() {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!reportingCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!pluginCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    return null;
  }

  @Override
  public String doDefault() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }
    normalizeContextPath();
    loadIssueCollectorSrc();
    loadPluginSettingAndParseResult();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }
    normalizeContextPath();
    loadIssueCollectorSrc();
    loadPluginSettingAndParseResult();

    if (getHttpRequest().getParameter("savesettings") != null) {
      String parseResult = parseSaveSettings(getHttpRequest());
      if (parseResult != null) {
        return parseResult;
      }
      savePluginSettings();
      setReturnUrl("/secure/ReportingWebAction!default.jspa");
      return getRedirect(INPUT);
    }
    setReturnUrl("/secure/admin/JiraTimetrackerReportingUserSettingsWebAction!default.jspa");
    return getRedirect(INPUT);
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public String getMessage() {
    return message;
  }

  public int getPageSize() {
    return pageSize;
  }

  public boolean getUserPopupVisible() {
    return userPopupVisible;
  }

  public boolean isWorklogTimeInSeconds() {
    return worklogTimeInSeconds;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  /**
   * Load the plugin settings and set the variables.
   */
  public void loadPluginSettingAndParseResult() {
    UserReportingSettingsHelper userReportingSettingsHelper =
        new UserReportingSettingsHelper(settingsFactory, JiraTimetrackerUtil.getLoggedUserName());
    pageSize = userReportingSettingsHelper.getPageSize();
    userPopupVisible = userReportingSettingsHelper.getIsShowTutorialDialog();
    worklogTimeInSeconds = userReportingSettingsHelper.getWorklogTimeInSeconds();

  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private void parsePageSizeInput(final String pageSizeInputValuse) {
    if (pageSizeInputValuse == null) {
      pageSize = DEFAULT_PAGE_SIZE;
    } else {
      pageSize = Integer.parseInt(pageSizeInputValuse);
    }
  }

  /**
   * Parse the request after the save button was clicked. Set the variables.
   *
   * @param request
   *          The HttpServletRequest.
   */
  public String parseSaveSettings(final HttpServletRequest request) {
    String pageSizeValue = request.getParameter("pageSizeInput");
    String userPopupVisibleValue = getHttpRequest().getParameter("user_popup");
    String worklogTimeInSecondsValue = getHttpRequest().getParameter("worklogTimeInSeconds");
    parseUserPopupVisible(userPopupVisibleValue);
    parsePageSizeInput(pageSizeValue);
    parseWorklogTimeInSeconds(worklogTimeInSecondsValue);
    return null;
  }

  private void parseUserPopupVisible(final String userPopupVisibleValue) {
    userPopupVisible = true;
    if (userPopupVisibleValue != null) {
      userPopupVisible = false;
    }
  }

  private void parseWorklogTimeInSeconds(final String worklogTimeInSecondsValue) {
    worklogTimeInSeconds = true;
    if ("default".equals(worklogTimeInSecondsValue)) {
      worklogTimeInSeconds = false;
    }
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  /**
   * Save the user reporting settings.
   */
  public void savePluginSettings() {
    UserReportingSettingsHelper userReportingSettingsHelper =
        new UserReportingSettingsHelper(settingsFactory, JiraTimetrackerUtil.getLoggedUserName());
    userReportingSettingsHelper.saveIsShowTutorialDialog(userPopupVisible);
    userReportingSettingsHelper.savePageSize(pageSize);
    userReportingSettingsHelper.saveWorklogTimeInSeconds(worklogTimeInSeconds);
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setIssueCollectorSrc(final String issueCollectorSrc) {
    this.issueCollectorSrc = issueCollectorSrc;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
  }

  public void setUserPopupVisible(final boolean userPopupVisible) {
    this.userPopupVisible = userPopupVisible;
  }

  public void setWorklogTimeInSeconds(final boolean worklogTimeInSeconds) {
    this.worklogTimeInSeconds = worklogTimeInSeconds;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
