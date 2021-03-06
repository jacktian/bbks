package com.fang.bbks.modules.social.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fang.bbks.common.utils.SessionUtil;
import com.fang.bbks.common.web.BaseController;
import com.fang.bbks.modules.social.entity.Dynamic;
import com.fang.bbks.modules.social.service.DynamicService;
import com.fang.bbks.modules.social.service.InterestService;
import com.fang.bbks.modules.social.service.MessageService;
import com.fang.bbks.modules.social.service.NetWorkService;
import com.fang.bbks.modules.social.service.RelationService;
import com.fang.bbks.modules.sys.entity.Comment;
import com.fang.bbks.modules.sys.entity.User;
import com.fang.bbks.modules.sys.service.CommentService;
import com.fang.bbks.modules.sys.service.UserService;

/**
 * @Intro descrption here
 * @author Lee [shouli1990@gmail.com]
 * @Version V0.0.1
 * @Date 2013-11-15
 * @since 下午1:59:52
 */

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {
	private final static Logger log = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private UserService userService;
	@Autowired
	private DynamicService dynamicService;
	@Autowired
	private NetWorkService netWorkService;
	@Autowired
	private RelationService relationService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private SessionUtil sessionUtil;
	@Autowired
	private CommentService commentService;
	@Autowired
	private InterestService interestService;
	/**
	 * 个人主页
	 * 
	 * @param request
	 * @param mv
	 * @return --> -->
	 */
	@RequestMapping(method = RequestMethod.GET, value = { "/profile/index","/profile/","/profile" })
	public String profile(Model uiModel,HttpServletRequest request,
			HttpServletResponse response) {
		
		User u = sessionUtil.getSignInUser(request.getSession());
		
		if(u == null){
			uiModel.addAttribute(HANDLER_MSG, "您还未登录，请登录");
			return "redirect:/login";
		}
		
		netWorkService.setUserInfo(u.getId(),uiModel);
		
		return "/user/profile";
	}

	/**
	 * 用户信息主页
	 * 
	 * @param request
	 * @param mv
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = { "/detail/{uid}" })
	public String detail(
			@PathVariable("uid") Long uid, Model uiModel,
			@RequestParam(value="type",required=false) Integer type,
			HttpServletRequest request) {		
		User cu =sessionUtil.getSignInUser(request.getSession());
		//未登录
		if(cu == null){
			uiModel.addAttribute(HANDLER_MSG, "您还未登录，请登录");
			return "redirect:/login";
		}
		
		//资源不存在，转首页
		if(userService.findOne(uid) == null){
			uiModel.addAttribute(HANDLER_MSG, "您要访问的资源已经被删除！");
			return "redirect:/index";
		}
		
		//转个人主页
		if(uid == cu.getId()){
			return "redirect:/user/profile";
		}
		
		if(type == null || type != 1 || type != 2){
			uiModel.addAttribute("dynamicInfo", dynamicService.listDynamic(uid));
			Comment comment = new Comment();
			comment.setUid(uid);
			uiModel.addAttribute("commentInfo", commentService.find(comment));
		}else if(type == 1){
			uiModel.addAttribute("dynamicInfo", dynamicService.listDynamic(uid));
		}else {
			Comment comment = new Comment();
			comment.setUid(uid);
			uiModel.addAttribute("commentInfo", commentService.find(comment));
		}
		uiModel.addAttribute("type", type);
		uiModel.addAttribute("userInfo", userService.findOne(uid));
		
		if(relationService.isFlow(cu.getId(), uid)){
			uiModel.addAttribute("doFlow", true);
		}
		
		//找他的兴趣
		uiModel.addAttribute("invos", interestService.findAll(uid, null));
		
		return "/user/detail";
	}
	
	/**
	 * 跟新用户状态信息
	 * @param uiModel
	 * @param request
	 * @param response
	 * @param description
	 * @return
	 */
	@RequestMapping(value = { "/updateStatus" })
	public String updateStatus( Model uiModel,HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "description",required = true) String description){
		
		log.debug("update description...{}",description);
		
		User u = sessionUtil.getSignInUser(request.getSession());
		if(u == null){
			uiModel.addAttribute(HANDLER_MSG, "您还未登录，请登录");
			return "redirect:/login";
		}
		
		try{
			userService.updateState(description,u.getId());
			
			u.setDescription(description);
			sessionUtil.setSignInUser(request.getSession(), u);
			
		}catch(Exception e){
			log.error("数据操作异常，{}",e.getMessage(),e);
			uiModel.addAttribute(HANDLER_MSG,"系统异常，稍后再试！");
		}
		return "redirect:/user/profile";
	}
	
	@RequestMapping(value = { "/updateAvatar" })
	public String updateAvatar( Model uiModel,HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "avatarSrc",required = true) String avatarSrc){
		
		log.debug("update avatarSrc...{}",avatarSrc);
		
		User u = sessionUtil.getSignInUser(request.getSession());
		if(u == null){
			uiModel.addAttribute(HANDLER_MSG, "您还未登录，请登录");
			return "redirect:/login";
		}
		
		try{
			userService.updateAvatar(avatarSrc,u.getId());
			
			u.setAvatar(avatarSrc);
			sessionUtil.setSignInUser(request.getSession(), u);
			
		}catch(Exception e){
			log.error("数据操作异常，{}",e.getMessage(),e);
			uiModel.addAttribute(HANDLER_MSG,"系统异常，稍后再试！");
		}
		return "redirect:/user/profile";
	}
	
	@RequestMapping(value = { "/publishDynamic" })
	public String publishDynamic( Model uiModel,HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "content",required = true) String content){
		
		log.debug("update content...{}",content);
		
		User u = sessionUtil.getSignInUser(request.getSession());
		if(u == null){
			uiModel.addAttribute(HANDLER_MSG, "您还未登录，请登录");
			return "redirect:/login";
		}
		
		try{
			dynamicService.publishDynamic(content, u.getId());
		}catch(Exception e){
			log.error("数据操作异常，{}",e.getMessage(),e);
			uiModel.addAttribute(HANDLER_MSG,"系统异常，稍后再试！");
		}
		return "redirect:/user/profile";
	}
	
	@RequestMapping(value = { "/flow" })
	public String flow( Model uiModel,HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "uid",required = true) Long uid){
		log.debug("flow...{}",uid);
		
		User u = sessionUtil.getSignInUser(request.getSession());
		if(u == null){
			uiModel.addAttribute(HANDLER_MSG, "您还未登录，请登录");
			return "redirect:/login";
		}
		try{
			netWorkService.flow(u.getId(), uid);
		}catch(Exception e){
			e.printStackTrace();
			log.error("err flow handler,{},{}", e.getMessage(),e);
		}
		
		return "redirect:/user/detail/"+uid;
	}
	
	@RequestMapping(value = { "/unflow" })
	public String unflow( Model uiModel,HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "uid",required = true) Long uid){
		log.debug("unflow...{}",uid);
		
		User u = sessionUtil.getSignInUser(request.getSession());
		if(u == null){
			uiModel.addAttribute(HANDLER_MSG, "您还未登录，请登录");
			return "redirect:/login";
		}
		try{
			netWorkService.unflow(u.getId(), uid);
		}catch(Exception e){
			e.printStackTrace();
			log.error("err flow handler,{},{}", e.getMessage(),e);
		}
		
		return "redirect:/user/detail/"+uid;
	}
	
	@RequestMapping(value = { "/sendMessage" })
	public String sendMessage( Model uiModel,
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "message",required = true) String message,
			@RequestParam(value = "uid",required = true) Long uid){
		log.debug("sendMessage...{}",uid);
		
		User u = sessionUtil.getSignInUser(request.getSession());
		if(u == null){
			uiModel.addAttribute(HANDLER_MSG, "您还未登录，请登录");
			return "redirect:/login";
		}
		
		//资源不存在，转首页
		if(userService.findOne(uid) == null){
			uiModel.addAttribute(HANDLER_MSG, "您要访问的资源已经被删除！");
			return "redirect:/index";
		}
		
		try{
			messageService.sendMessage(message, u.getId(), uid);
		}catch(Exception e){
			e.printStackTrace();
			log.error("err flow handler,{},{}", e.getMessage(),e);
		}
		
		return "redirect:/user/profile";
	}
	
	@RequestMapping(value = { "/replyMessage" })
	public String replyMessage( Model uiModel,
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "messageId",required = true) Long messageId,
			@RequestParam(value = "message",required = true) String message,
			@RequestParam(value = "uid",required = true) Long uid){
		log.debug("replyMessage...{},{},{}",uid,message,messageId);
		
		User u = sessionUtil.getSignInUser(request.getSession());
		if(u == null){
			uiModel.addAttribute(HANDLER_MSG, "您还未登录，请登录");
			return "redirect:/login";
		}
		
		//资源不存在，转首页
		if(userService.findOne(uid) == null){
			uiModel.addAttribute(HANDLER_MSG, "您要访问的资源已经被删除！");
			return "redirect:/index";
		}
		
		try{
			messageService.replyMessage(u.getId(), uid,message,messageId);
		}catch(Exception e){
			e.printStackTrace();
			log.error("err reply handler,{},{}", e.getMessage(),e);
		}
		
		return "redirect:/user/profile";
	}
	
}
