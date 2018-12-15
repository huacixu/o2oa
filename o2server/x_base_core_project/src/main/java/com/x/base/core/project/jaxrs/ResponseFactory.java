package com.x.base.core.project.jaxrs;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import com.x.base.core.project.exception.CallbackPromptException;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.CacheControlFactory;
import com.x.base.core.project.tools.DefaultCharset;

public class ResponseFactory {

	private static CacheControl defaultCacheControl = CacheControlFactory.getDefault();

	public static final String Content_Disposition = "Content-Disposition";
	// public static final String Content_Length = "Content-Length";
	public static final String Accept_Ranges = "Accept-Ranges";
	public static final String Content_Type = "Content-Type";

	public static <T> Response getDefaultActionResultResponse(ActionResult<T> result) {
		if (result.getType().equals(ActionResult.Type.error)) {
			if ((result.throwable instanceof CallbackPromptException)) {
				return Response.ok(callbackError(result)).cacheControl(defaultCacheControl).build();
			} else {
				return Response.serverError().entity(result.toJson()).cacheControl(defaultCacheControl).build();
			}
		} else {
			if ((null != result.getData()) && (result.getData() instanceof WoFile)) {
				WoFile wo = (WoFile) result.getData();
				return Response.ok(wo.getBytes()).header(Content_Disposition, wo.getContentDisposition())
						.header(Content_Type, wo.getContentType()).header(Accept_Ranges, "bytes").build();
			} else if ((null != result.getData()) && (result.getData() instanceof WoText)) {
				WoText wo = (WoText) result.getData();
				return Response.ok(wo.getText()).cacheControl(defaultCacheControl).build();
			} else if ((null != result.getData()) && (result.getData() instanceof WoCallback)) {
				return Response.ok(callback((WoCallback) result.getData())).cacheControl(defaultCacheControl).build();
			} else {
				return Response.ok(result.toJson()).cacheControl(defaultCacheControl).build();
			}
		}
	}

	private static String callback(WoCallback woCallback) {
		ActionResult<Object> result = new ActionResult<>();
		result.setData(woCallback.getObject());
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head><meta http-equiv=\"charset\" content=\"" + DefaultCharset.name + "\" /><script>");
		sb.append(woCallback.getCallbackName() + "('" + result.toCompactJson() + "')");
		sb.append("</script></head></html>");
		return sb.toString();
	}

	private static String callbackError(ActionResult result) {
		StringBuffer sb = new StringBuffer();
		CallbackPromptException ex = (CallbackPromptException) result.throwable;
		sb.append("<html><head><meta http-equiv=\"charset\" content=\"" + DefaultCharset.name + "\" /><script>");
		sb.append(ex.getCallbackName() + "('" + result.toCompactJson() + "')");
		sb.append("</script></head></html>");
		return sb.toString();
	}
}
