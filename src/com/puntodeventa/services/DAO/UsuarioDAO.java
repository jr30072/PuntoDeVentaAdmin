package com.puntodeventa.services.DAO;

import com.puntodeventa.global.Entity.Usuario;
import com.puntodeventa.global.Util.LogHelper;
import com.puntodeventa.global.Util.ValidacionForms;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleTypes;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Fortunato Hdez Hdez
 */
public class UsuarioDAO {

    ValidacionForms vali = new ValidacionForms();
    LogHelper objLog = new LogHelper("UsuarioDAO");
    ConnectedByHibernate cBH = new ConnectedByHibernate();
    private Session session;
    private Transaction tx;
    Connection con;
    CallableStatement cs = null;
    ResultSet rs;

    //Metodo: Inicializa la operacion para procesos en la base de datos
    private void iniciaOperacion() {
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
        } catch (HibernateException ex) {
            objLog.Log(ex.getMessage());
        }
    }

    // Metodo: Obtenemos el error  que podria ocacionar an cada metodo
    private void manejaException(HibernateException he) throws HibernateException {
        tx.rollback();
        objLog.Log(he.getMessage());
        throw new HibernateException("Ocurrio un error en la capa de acceso a datos. Error: " + he.getMessage());
    }

    //Metodo Guarda Usuarioo
    public int saveUsuario(Usuario usuario) {
        int id_usuario = 0;
        try {
            this.iniciaOperacion();
            id_usuario = Integer.parseInt(session.save(usuario).toString());
            tx.commit();
            vali.msjInfo("Registro guardado correctamente...");
        } catch (HibernateException he) {
            this.manejaException(he);
            throw he;
        } finally {
            session.close();
        }
        return id_usuario;
    }

    /**
     * Actualiza los datos de la tabla Usuario
     */
    public void updateUsuario(Usuario usuario) throws HibernateException {
        try {
            this.iniciaOperacion();
            session.update(usuario);
            tx.commit();
        } catch (HibernateException he) {
            this.manejaException(he);
            throw he;
        } finally {
            session.close();
        }
    }

    /**
     * Elimina registro de la Tabla Usuario
     */
    public void deleteUsuario(Usuario usuario) {
        try {
            this.iniciaOperacion();
            session.delete(usuario);
            tx.commit();
        } catch (HibernateException he) {
            this.manejaException(he);
            throw he;
        } finally {
            session.close();
        }
    }

    /**
     * Metodo que devuelve un product
     */
    public Usuario selectUsuario(int idUsuario) {
        Usuario usuario = null;
        try {
            this.iniciaOperacion();
            usuario = (Usuario) session.get(Usuario.class, idUsuario);            
        } catch (HibernateException he) {
            this.manejaException(he);
            throw he;
        } finally {
            session.close();
        }
        return usuario;
    }
    private static String query = "";

    public List<Usuario> listUsuario() {
        List<Usuario> listUsuario = null;

        try {
            this.iniciaOperacion();
            final SQLQuery sql = session.createSQLQuery(query).addEntity(Usuario.class);
            listUsuario = sql.list();
        } finally {
        }
        return listUsuario;
    }

    public List<Usuario> logonUsuario(String user, String pwd) {
        
        List<Usuario> usuario = null;
        try {
            this.iniciaOperacion();
            final SQLQuery sql = session.createSQLQuery(
                    "SELECT * FROM VT_USUARIOS"
                    + " WHERE  CVE_USUARIO = '" + user + "'"
                    + " AND CONTRASENA = '" + pwd + "'").addEntity(Usuario.class);
            usuario = sql.list();

        } catch (HibernateException he) {
            this.manejaException(he);
            throw he;
        } finally {
            session.close();
        }
        return usuario;
    }

    public String getAcceso(String user, String password, String codePOS){
        String accesoPOS = null;
        String msj;
        try{
            con = cBH.getConectionJDBC();
            if (null!=con){
                System.out.println("Conexion realizada");
                cs = con.prepareCall("BEGIN VT.PUNTO_VENTA.GET_ACCESO(?,?,?,?,?); END;");
                cs.setString("P_USUARIO", user);
                cs.setString("P_CONTRASENA", password);
                cs.setString("P_CVE_SISTEMA", "1");
                cs.registerOutParameter("P_ACCESO", OracleTypes.VARCHAR);
                cs.registerOutParameter("P_MSJ", OracleTypes.VARCHAR);
                cs.execute();
                accesoPOS = (String)cs.getString("P_ACCESO");
                msj = (String)cs.getString("P_MSJ");
            }
        }catch(Exception ex){
            return accesoPOS;
        }
        return accesoPOS;
    }
}