package com.databox.entity.query;

import java.util.Date;


/**
 * 用户操作日志表参数
 */
public class SysOpLogQuery extends BaseParam {


	/**
	 * 日志ID
	 */
	private Integer logId;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 用户名
	 */
	private String userName;

	private String userNameFuzzy;

	/**
	 * 操作模块（如：文件管理）
	 */
	private String module;

	private String moduleFuzzy;

	/**
	 * 操作动作（如：删除文件）
	 */
	private String action;

	private String actionFuzzy;

	/**
	 * 状态 1:成功 0:失败
	 */
	private Integer status;

	/**
	 * 返回信息或异常提示
	 */
	private String resultMsg;

	private String resultMsgFuzzy;

	/**
	 * 操作时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 操作详情(如：操作的文件名称列表)
	 */
	private String detail;

	private String detailFuzzy;


	public void setLogId(Integer logId){
		this.logId = logId;
	}

	public Integer getLogId(){
		return this.logId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setUserIdFuzzy(String userIdFuzzy){
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy(){
		return this.userIdFuzzy;
	}

	public void setUserName(String userName){
		this.userName = userName;
	}

	public String getUserName(){
		return this.userName;
	}

	public void setUserNameFuzzy(String userNameFuzzy){
		this.userNameFuzzy = userNameFuzzy;
	}

	public String getUserNameFuzzy(){
		return this.userNameFuzzy;
	}

	public void setModule(String module){
		this.module = module;
	}

	public String getModule(){
		return this.module;
	}

	public void setModuleFuzzy(String moduleFuzzy){
		this.moduleFuzzy = moduleFuzzy;
	}

	public String getModuleFuzzy(){
		return this.moduleFuzzy;
	}

	public void setAction(String action){
		this.action = action;
	}

	public String getAction(){
		return this.action;
	}

	public void setActionFuzzy(String actionFuzzy){
		this.actionFuzzy = actionFuzzy;
	}

	public String getActionFuzzy(){
		return this.actionFuzzy;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setResultMsg(String resultMsg){
		this.resultMsg = resultMsg;
	}

	public String getResultMsg(){
		return this.resultMsg;
	}

	public void setResultMsgFuzzy(String resultMsgFuzzy){
		this.resultMsgFuzzy = resultMsgFuzzy;
	}

	public String getResultMsgFuzzy(){
		return this.resultMsgFuzzy;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

	public void setDetail(String detail){
		this.detail = detail;
	}

	public String getDetail(){
		return this.detail;
	}

	public void setDetailFuzzy(String detailFuzzy){
		this.detailFuzzy = detailFuzzy;
	}

	public String getDetailFuzzy(){
		return this.detailFuzzy;
	}

}
