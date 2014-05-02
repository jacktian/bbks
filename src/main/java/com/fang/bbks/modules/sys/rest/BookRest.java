package com.fang.bbks.modules.sys.rest;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fang.bbks.common.persistence.JsonResult;
import com.fang.bbks.common.persistence.Page;
import com.fang.bbks.common.utils.Collections3;
import com.fang.bbks.common.web.BaseController;
import com.fang.bbks.modules.sys.entity.Book;
import com.fang.bbks.modules.sys.service.BookService;
import com.google.common.collect.Lists;

/**
 * @Intro descrption here
 * @author Lee [shouli1990@gmail.com]
 * @Version V0.0.1
 * @Date 2014-5-2
 * @since 下午6:40:53	
 */

@Controller
@RequestMapping(value = BaseController.REST_PREFIX+"/book")
public class BookRest extends BaseController{
	
	@Resource
	BookService bs;
	
	@RequestMapping(value={"/find"},method={RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody String find(
			@RequestParam(value="catlog" , required = false) Long catlog){
		JsonResult jr = new JsonResult();
		
		Page<Book> page = new Page<Book>(request, response);
		page = bs.findBook(page, catlog);
		if(page != null && !Collections3.isEmpty(page.getList())){
			jr.setIsSuccess(Boolean.TRUE);
			jr.setRows(page.getList());
			jr.setMessage("成功！");
		}else{
			jr.setIsSuccess(Boolean.FALSE);
			jr.setRows(Lists.newArrayList());
			jr.setMessage("成功！");
		}
		return jr.toJson();
	}
	
	@RequestMapping(value={"/search"},method={RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody String search(
			@RequestParam(value="keywords" , required = false) String keywords){
		JsonResult jr = new JsonResult();
		
		Page<Book> page = new Page<Book>(request, response);
		page = bs.findByKeyWords(page, keywords);
		if(page != null && !Collections3.isEmpty(page.getList())){
			jr.setIsSuccess(Boolean.TRUE);
			jr.setRows(page.getList());
			jr.setMessage("成功！");
		}else{
			jr.setIsSuccess(Boolean.FALSE);
			jr.setRows(Lists.newArrayList());
			jr.setMessage("成功！");
		}
		return jr.toJson();
	}
	
	@RequestMapping(value={"/findOne"},method={RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody String findOne(
			@RequestParam(value="bookId" , required = false) Long bookId){
		
		JsonResult jr = new JsonResult();
		Book book = bs.findOne(bookId);
		
		if(book != null){
			jr.setIsSuccess(Boolean.TRUE);
			jr.setObj(book);
			jr.setMessage("成功！");
		}else{
			jr.setIsSuccess(Boolean.FALSE);
			jr.setMessage("成功！");
		}
		return jr.toJson();
	}

}