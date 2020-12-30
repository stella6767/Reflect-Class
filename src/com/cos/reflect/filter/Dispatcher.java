package com.cos.reflect.filter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

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
import com.cos.reflect.controller.dto.LoginDto;

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
			RequestMapping requestMapping = (RequestMapping) annotation; //.value 호출하기 위해 다운캐스팅
			System.out.println(requestMapping.value());
			 //위에서부터 아래로 찾음
			if(requestMapping.value().equals(endPoint)) {
				try {
					System.out.println("들왔나?");
					Parameter[] params = method.getParameters();//login(LoginDto dto) 여기가 왜 0?
					String path = null;  
				
					System.out.println(params.length);
					
					if(params.length != 0) {	
					
						System.out.println("params[0].getType() : "+params[0].getType());
						//Object dtoInstance = params[0].getType().newInstance(); // /user/login => LoginDto, /user/join => JoinDto
						// 해당 오브젝트를 리플렉션해서 set함수 호출
//						Object dtoInstance = params[0].getType().newInstance();
//						LoginDto.class.newInstance();
						Object dtoInstance = Class.forName(params[0].getType().toString()); //newinstance가 안 먹힘
						
						String username = request.getParameter("username");
						String password = request.getParameter("password");
						System.out.println("username : "+username);
						System.out.println("password : "+password);
						
						Enumeration<String> keys = request.getParameterNames(); //username, password 크기가 2
						// keys 값을 변형 username => setUsername
						// keys 값을 변형 password => setPassword
						
						while(keys.hasMoreElements()) {
							System.out.println(keys.nextElement());
						}
						path = "/";
										
					}else {						
						path = (String)method.invoke(userController);						
					}
					
					RequestDispatcher dis = request.getRequestDispatcher(path); // 필터를 다시 안탐!!
					dis.forward(request, response);

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		
		}
		
	}

}
