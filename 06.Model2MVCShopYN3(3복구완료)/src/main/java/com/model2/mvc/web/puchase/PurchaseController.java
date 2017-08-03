package com.model2.mvc.web.puchase;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.model2.mvc.common.CommonUtil;
import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.purchase.PurchaseService;
import com.model2.mvc.service.user.UserService;

@Controller
public class PurchaseController {

	///Field
	@Autowired
	@Qualifier("purchaseServiceImpl")
	private PurchaseService purchaseService;
	
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	
	@Autowired
	@Qualifier("userServiceImpl")
	private UserService userService;
	
	///Constructor
	public PurchaseController(){
		System.out.println(this.getClass());
	}
	
	@Value("#{commonProperties['pageUnit']}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']}")
	int pageSize;
	
	
	@RequestMapping("/addPurchaseView.do")
	public String addPurchaseView(@RequestParam("prodNo") String prodNo,
																		Model model ) throws Exception{
		System.out.println("/addPurchaseView.do");
		
		Product product = productService.getProduct(Integer.parseInt(prodNo));
		model.addAttribute("product", product);
		
		return "forward:/purchase/addPurchaseView.jsp";
	}
	
	
	@RequestMapping("/addPurchase.do")
	public String addPurchase(@RequestParam("prodNo") String prodNo,
												    	@RequestParam("buyerId") String buyerId,
												    	@ModelAttribute("purchase") Purchase purchase,
												    	Model model) throws Exception{
		System.out.println("/addPurchase.do");
		
		purchase.setBuyer(userService.getUser(buyerId));
		int prodNoInt = Integer.parseInt(prodNo);
		purchase.setPurchaseProd(productService.getProduct(prodNoInt));
		purchase.setTranCode("1");
		purchaseService.addPurchase(purchase);
		
		model.addAttribute("prodNo", prodNo);
		model.addAttribute("purchase", purchase);
		model.addAttribute("buyerId", buyerId);
		
		return "forward:/purchase/purchaseComplete.jsp";
	}
	
	@RequestMapping("/getPurchase.do")
	public String getPurchase(@RequestParam("tranNo") int tranNo, //string/int?
																		Model model) throws Exception{
		System.out.println("/getPurchase.do");
		
		Purchase purchase = purchaseService.getPurchase(tranNo);
		purchase.setDivyDate(CommonUtil.toDateStr(purchase.getDivyDate()));
		model.addAttribute("purchase", purchase);

		return "forward:/purchase/readPurchase.jsp";
	}
	
	@RequestMapping("/listPurchase.do")
	public String listPurchase(@ModelAttribute("search") Search search,
															HttpSession session, Model model) throws Exception{
		System.out.println("/listPurchase.do");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		User user = (User) session.getAttribute("user");
		search.setSearchKeyword(user.getUserId());
		Map<String , Object> map=purchaseService.getPurchaseList(search, user.getUserId());
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println("resultPage : " +resultPage);
		
		model.addAttribute("buyerId", user.getUserId());
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);

		return "forward:/purchase/listPurchase.jsp";
	}	
	
	@RequestMapping("/updatePurchaseView.do")
	public String updatePurchaseView(@RequestParam("tranNo") int tranNo,
																Model model) throws Exception{
		System.out.println("/updatePurchaseView.jsp");
		
		Purchase purchase=purchaseService.getPurchase(tranNo);
		model.addAttribute("purchase", purchase);
		
		return "forward:/purchase/updatePurchase.jsp";
	}
	
	@RequestMapping("/updatePurchase.do")
	public String updatePurchase(@RequestParam("tranNo") int tranNo,
															 @ModelAttribute("purchase") Purchase purchase,
																Model model) throws Exception{
		System.out.println("/updatePurchase.do");
		
		purchase.setTranNo(tranNo);
		purchaseService.updatePurchase(purchase);
		
		model.addAttribute("tranNo", tranNo);

		return "redirect:/getPurchase.do";
	}
	
	@RequestMapping("/updateTranCode.do")
	public String updateTranCode(@RequestParam("tranNo") int tranNo,
																@RequestParam("tranCode") String tranCode,
																Model model) throws Exception{
		System.out.println("/updateTranCode.do");
		
		Purchase purchase = purchaseService.getPurchase(tranNo);
		purchase.setTranCode(tranCode);
		purchaseService.updateTranCode(purchase);

		return "redirect:/listPurchase.do";
	}
	
	@RequestMapping("/updateTranCodeByProd.do")
	public String updateTranCodeByProd(@RequestParam("prodNo") int prodNo,
																			@RequestParam("tranCode") String tranCode,
																			Model model) throws Exception{
		System.out.println("/updateTranCodeByProd.do");
		
		Purchase purchase = purchaseService.getPurchase2(prodNo);
		purchase.setTranCode(tranCode);
		purchaseService.updatePurchase(purchase);
		
		model.addAttribute("menu", "manage");

		return "redirect:/listProduct.do";
	}
}
