package com.cos.reflect.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
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

public class Dispatcher implements Filter {
	
	private boolean isMatching = false;

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

		for (Method method : methods) { // 4바퀴 (join, login, user, hello)
			Annotation annotation = method.getDeclaredAnnotation(RequestMapping.class);
			RequestMapping requestMapping = (RequestMapping) annotation; //.value 호출하기 위해 다운캐스팅
			
			
			 //위에서부터 아래로 찾음
			if(requestMapping.value().equals(endPoint)) {
				try {
	
					Parameter[] params = method.getParameters();//login(LoginDto dto) 여기가 왜 0?
					String path = null;  
					System.out.println(params.length);
					
					if(params.length != 0) {	
					
						System.out.println("params[0].getType() : "+params[0].getType());
						//Object dtoInstance = params[0].getType().newInstance(); // /user/login => LoginDto, /user/join => JoinDto						
						Object dtoInstance = params[0].getType().getDeclaredConstructor().newInstance();
						// 해당 오브젝트를 리플렉션해서 set함수 호출
						//Object dtoInstance = Class.forName(params[0].getType().toString()); //newinstance가 안 먹힘
						
						setData(dtoInstance, request);
						path = (String)method.invoke(userController, dtoInstance);
										
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
		
		if (isMatching == false) {

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.println("잘못된 주소 요청입니다. 404 에러");
			out.flush();

		}
		


		
	}
	
	
	private <T> void setData(T instance, HttpServletRequest request) {
		Enumeration<String> keys = request.getParameterNames(); // 크기 : 2 (username, password)
		while (keys.hasMoreElements()) { // 2번 돈다.
			String key = (String) keys.nextElement();
			String methodKey = keyToMethodKey(key); // setUsername

			Method[] methods = instance.getClass().getDeclaredMethods(); // 5개

			for (Method method : methods) {
				if (method.getName().equals(methodKey)) {
					try {
						method.invoke(instance, request.getParameter(key));
					} catch (Exception e) {
						e.printStackTrace();
						try {
							int value = Integer.parseInt(request.getParameter(key));
							method.invoke(instance, value);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
						System.out.println("신경쓸 필요없는 별거 아닌 int 파싱 문제");
					}
					
					
				}
			}
		}
	}
	
	private String keyToMethodKey(String key) {
		String firstKey = "set";
		String upperKey = key.substring(0, 1).toUpperCase();
		String remainKey = key.substring(1);

		return firstKey + upperKey + remainKey;
	}

}
