package sk.tuke.juho.persistence; /**
 * Created by Juraj on 7.3.2016.
 */

import java.lang.reflect.*;

class ProxyHelper implements InvocationHandler {

    static Object createProxy(Class clas, int idObject, PersistenceManager pm, Field field, Object obj) {
        return Proxy.newProxyInstance(clas.getClassLoader(),
                clas.getInterfaces(),
                new ProxyHelper(idObject, pm, clas, field, obj));
    }

    private int id;
    private Object target;
    private PersistenceManager pm;
    private Class objClass;
    private Field parentsField;
    private Object parent;

    private ProxyHelper(int idObject, PersistenceManager persistenceManager, Class objClass, Field field, Object obj) {
        this.target = null;
        this.pm = persistenceManager;
        this.id = idObject;
        this.objClass = objClass;
        this.parent = obj;
        this.parentsField = field;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        System.out.println(method.getName()+"**"+objClass.getSimpleName()+"**");
        try {
            if(target==null){
                target = pm.get(objClass, id);
                parentsField.setAccessible(true);
                parentsField.set(parent,target);
            }
            result = method.invoke(target,args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }
}

