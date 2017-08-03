package com.model2.mvc.web.product;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;

@Controller
public class ProductController {

	///Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	
	///Constructor
	public ProductController(){
		System.out.println(this.getClass());
	}
	
	@Value("#{commonProperties['pageUnit']}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']}")
	int pageSize;
	
	
	@RequestMapping("/addProductView.do")
	public ModelAndView addProductView() throws Exception{
		System.out.println("/addProductView.do");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("/product/addProductView.jsp"); //forward: default
		return modelAndView;
	}
	
	@RequestMapping("/addProduct.do")
	public ModelAndView addProduct(@ModelAttribute("product") Product product,
															Model model) throws Exception{
		System.out.println("/addProduct.do");
		
		product.setManuDate(product.getManuDate().replaceAll("-",""));
		
		productService.addProduct(product);
		model.addAttribute("product", product);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("/product/checkProduct.jsp");
		return modelAndView;
		
	}
	
	@RequestMapping("/getProduct.do")
	public ModelAndView getProduct(@RequestParam("prodNo") int prodNo,
																		@RequestParam("menu") String menu,
																		HttpServletRequest request,
																		HttpServletResponse response,
																		Model model) throws Exception{
		System.out.println("/getProduct.do");
		
		String newCookie = null;
		
		Cookie[] cookies = request.getCookies();
		if (cookies!=null && cookies.length > 0) {
				for (int i = 0; i < cookies.length; i++) {
					Cookie cookie = cookies[i];
					if (cookie.getName().equals("history")) {
						newCookie = cookie.getValue()+","+prodNo;
						System.out.println("newCookie의 값은 "+newCookie);
						cookie.setValue(newCookie);
					    response.addCookie(cookie);
					}
					else{
						cookie = new Cookie("history", prodNo+"");
					    response.addCookie(cookie);
					}
				}
		}
		
		Product product = productService.getProduct( prodNo );
		
		ModelAndView modelAndView = new ModelAndView();
		if(menu.equals("manage")){
			model.addAttribute("prodNo", prodNo);
			modelAndView.setViewName("/updateProductView.do");
		}else{
			model.addAttribute("product", product);
			modelAndView.setViewName("/product/readProduct.jsp");
		}
	
		return modelAndView;
	}
	
	@RequestMapping("/updateProductView.do")
	public ModelAndView updateProductView(@RequestParam("prodNo") int prodNo,
																Model model) throws Exception{
		System.out.println("/updateProductView.jsp");
		
		model.addAttribute("product", productService.getProduct(prodNo));

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("/product/updateProduct.jsp");
		return modelAndView;
	}
	
	@RequestMapping("/updateProduct.do")
	public ModelAndView updateProduct(@ModelAttribute("product") Product product,
																Model model) throws Exception{
		System.out.println("/updateProduct.do");
		
		product.setManuDate(product.getManuDate().replaceAll("-",""));
		productService.updateProduct(product);
		
		model.addAttribute("prodNo",product.getProdNo() );
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("/product/getProduct.jsp");
		return modelAndView;

	}
	
	@RequestMapping("/listProduct.do")
	public ModelAndView listProduct(@ModelAttribute("search") Search search,
															@RequestParam("menu") String menu,
															Model model) throws Exception{
		System.out.println("/listProduct.do");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		Map<String , Object> map=productService.getProductList(search);
		
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		
		model.addAttribute("menu", menu);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("/product/listProduct.jsp");
		return modelAndView;
	}
	
}
