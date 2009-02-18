package org.hippoecm.hst.core.container;

import javax.servlet.ServletRequest;

public class ResourceServingValve extends AbstractValve {
    
    @Override
    public void invoke(ValveContext context) throws ContainerException {

        if (!context.getServletResponse().isCommitted() && isResourceRequest()) {
            
            HstComponentWindow window = findResourceServingWindow(context.getRootComponentWindow());
            
            if (window != null) {
                ServletRequest servletRequest = context.getServletRequest();
                HstComponentInvoker invoker = getComponentInvoker();
                invoker.invokeBeforeServeResource(context.getServletContext(), servletRequest, context.getServletResponse());
                invoker.invokeServeResource(context.getServletContext(), servletRequest, context.getServletResponse());
            }
        }
        
        // continue
        context.invokeNext();
    }

    private HstComponentWindow findResourceServingWindow(HstComponentWindow rootComponentWindow) {
        // TODO Auto-generated method stub
        return null;
    }
}
