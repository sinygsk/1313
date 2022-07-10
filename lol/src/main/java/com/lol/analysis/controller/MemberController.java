package com.lol.analysis.controller;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.lol.analysis.repository.BoardRepository;
import com.lol.analysis.repository.MemberRepository;
import com.lol.analysis.vo.BoardVo;
import com.lol.analysis.vo.MemberVo;
//import com.shop.spring_study.vo.ItemVo;

@Controller
@RequestMapping("/member")
public class MemberController {
	@Autowired
	MemberRepository mr;
	
	@Autowired
	BoardRepository br;
	
	@GetMapping("/list.do")
	public String list(Model model) {
		Iterable<MemberVo> memList = mr.findAll();
		model.addAttribute("memList", memList);
		System.out.println(memList);
		return "member/list";
	}
	
	@GetMapping("/login.do")
	public ModelAndView login(ModelAndView model) {
		model.setViewName("/member/login");
		return model;
	}
	
	@PostMapping("/login.do")
	public String login(String id, String pw, HttpSession session) {
		MemberVo memberVo=mr.findByIdAndPw(id, pw);
		if(memberVo!=null) {
			session.setAttribute("memberVo", memberVo);
			return "redirect:/";
		}else {
			return "redirect:/member/login";
		}
	}
	
	@GetMapping("/logout.do")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}
	
	@GetMapping("/signup.do")
	public ModelAndView signup(ModelAndView model) {
		model.setViewName("/member/signup");
		return model;
	}
	
	@PostMapping("/signup.do")
    public String signup(MemberVo memberVo,HttpSession session) {
        boolean insert=false;
        try {
            Optional<MemberVo> memOption=mr.findById(memberVo.getId()); //id중복확인
            if(memOption.isEmpty()) {
                MemberVo insertMem=mr.save(memberVo);
                if(insertMem!=null) {insert=true;} //회원가입 성공
            }else {
                session.setAttribute("msg", "존재하는 아이디 입니다.");
            }
        }catch(Exception e){
            e.printStackTrace();
            session.setAttribute("msg", "Email이나 phone이 존재합니다.");
        }
        if(insert) {
            return "redirect:/";
        }else {
            return "redirect:/member/signup.do";
        }
    }
	
	@GetMapping("/list/{page}")
	public String pageableList( @PathVariable int page,
								@RequestParam(defaultValue = "postTime") String sort,
								@RequestParam(defaultValue = "desc") String desc ,
								Model model) {
		int size=5;
		Pageable pageable=null;
		if(desc.equals("desc")) {
			pageable=PageRequest.of(page-1, size, Sort.by(sort).descending()); //mysql limit(start,size)			
		}else if(desc.equals("asc")) {
			pageable=PageRequest.of(page-1, size, Sort.by(sort).ascending()); //mysql limit(start,size)			
		}
		//모든 jpa 함수에 아무런 제약없이 Pageable를 매개변수로 정의할 수 있고
		//Iteralbe을 부모로 하고 totalPage nextPage total과 같이 page에 필요한 정보를 수집하는 필드를 갖는 Page를 Return 받을 수 있다.
		
		Page<BoardVo> itemList=br.findAll(pageable);
		
		
		
		model.addAttribute("memberList", itemList);
		return "/member/pageableList";
	}
	
	@GetMapping("/ajax/findId/{id}")
    public @ResponseBody Optional<MemberVo> findId(@PathVariable String id) {
        return mr.findById(id);
    }
    @GetMapping("/ajax/findEmail/{email}")
    public @ResponseBody Optional<MemberVo> findEmail(@PathVariable String email) {
        return mr.findByEmail(email);
    }
    
    @GetMapping("/ajax/findPhone/{phone}")
    public @ResponseBody Optional<MemberVo> findPhone(@PathVariable String phone) {
        return mr.findByPhone(phone);
    }
}
