package com.cos.reflect.filter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cos.reflect.anno.RequestMapping;
import com.cos.reflect.controller.UserController;

public class Dispatcher implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// 주소 파싱하기
		String endPoint = request.getRequestURI().replaceAll(request.getContextPath(), "");
		System.out.println("엔드포인트 " + endPoint);

		UserController userController = new UserController();

		// 리플렉션 -> 메서드를 런타임 시점에서 찾아내서 실행
		Method[] methods = userController.getClass().getDeclaredMethods();// 그 파일에 메서드만!

//		for (Method method : methods) {
//			// System.out.println(method.getName());
//			if (endPoint.equals("/" + method.getName())) {
//				try {
//					method.invoke(userController);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}

		
		for (Method method : methods) { // 4바퀴 (join, login, user, hello)
			Annotation annotation = method.getDeclaredAnnotation(RequestMapping.class);
			RequestMapping requestMapping = (RequestMapping) annotation;
			System.out.println(requestMapping.value());
			 //위에서부터 아래로 찾음
			if(requestMapping.value().equals(endPoint)) {
				try {
					String path = (String)method.invoke(userController);// '/' 받음
					System.out.println(path);
					RequestDispatcher dis = request.getRequestDispatcher(path);
					dis.forward(request, response);//RequestDispatcher를 사용하면, 
					//내부적으로 전달하기 때문에, web.xml를 다시 타지 않는다.
					//여기서 함수를 실행해서 받은 request, response 객체를 그대로 전송한다. 
					
					
					//response.sendRedirect("index.jsp");//만약 sendredirect를 한다면, req,resp
					//객체를 새로 생성하는 것이기 때문에 다시 web.xml를 타고 들어옴
					//그래서 필터에 걸림.
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		
		}
		
	}

}
