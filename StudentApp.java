import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class StudentApp {

    static List<String[]> students = new ArrayList<>();
    static int idCounter = 1;

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);

        server.createContext("/", new Welcome());
        server.createContext("/login", new Login());
        server.createContext("/dashboard", new Dashboard());
        server.createContext("/add", new AddStudent());
        server.createContext("/view", new ViewStudents());
        server.createContext("/update", new UpdateStudent());
        server.createContext("/delete", new DeleteStudent());
        server.createContext("/review", new Review());

        server.start();
        System.out.println("Running → http://localhost:9090");
    }

    static String style =
        "<style>" +
        "body{font-family:Arial;margin:0;background:linear-gradient(#74ebd5,#9face6);text-align:center;}" +
        ".box{width:420px;margin:auto;margin-top:60px;background:white;padding:20px;border-radius:12px;box-shadow:0 0 15px rgba(0,0,0,0.2);}" +
        "input{width:90%;padding:10px;margin:8px;border-radius:5px;border:1px solid #ccc;}" +
        "button{width:95%;padding:10px;margin:6px;background:#3498db;color:white;border:none;border-radius:5px;cursor:pointer;}" +
        ".card{background:white;padding:15px;margin:15px;border-radius:10px;box-shadow:0 3px 10px rgba(0,0,0,0.2);text-align:left;}" +
        ".btn{padding:8px 12px;margin:4px;border:none;border-radius:5px;color:white;}" +
        ".update{background:#1e90ff;}" +
        ".delete{background:#ff4757;}" +
        "</style>";

    // ✅ UPDATED WELCOME PAGE
    static class Welcome implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            send(t,
                "<html>"+style+"<body><div class='box'>" +
                "<h1>Welcome</h1>" +
                "<h2>StudentApp</h2>" +
                "<a href='/login'><button>Start</button></a>" +
                "</div></body></html>"
            );
        }
    }

    // LOGIN (same)
    static class Login implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            send(t, "<html>"+style+"<body><div class='box'><h2>Admin Login</h2>" +
                    "<input placeholder='Username'><input type='password' placeholder='Password'>" +
                    "<a href='/dashboard'><button>Login</button></a>" +
                    "<a href='/'><button>Back</button></a></div></body></html>");
        }
    }

    // ✅ UPDATED DASHBOARD (SUBJECT MARKS ADDED)
    static class Dashboard implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            send(t, "<html>"+style+"<body><div class='box'><h2>STUDENT DASHBOARD</h2>" +
                    "<form action='/add'>" +
                    "<input name='name' placeholder='Name' required>" +
                    "<input name='faculty' placeholder='Faculty' required>" +
                    "<input name='prn' placeholder='PRN No' required>" +
                    "<input name='email' placeholder='Email' required>" +

                    "<h3>Enter Marks</h3>" +
                    "<input name='devops' placeholder='DevOps'>" +
                    "<input name='java' placeholder='Java'>" +
                    "<input name='python' placeholder='Python'>" +
                    "<input name='aws' placeholder='AWS'>" +
                    "<input name='android' placeholder='Android'>" +

                    "<button>Add Student</button></form>" +
                    "<a href='/view'><button>View Records</button></a>" +
                    "<a href='/review'><button>Review</button></a>" +
                    "<a href='/login'><button>Logout</button></a></div></body></html>");
        }
    }

    // ✅ UPDATED ADD (NOW STORES SUBJECTS)
    static class AddStudent implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String q = t.getRequestURI().getQuery();
            if (q != null) {
                Map<String,String> m = map(q);

                students.add(new String[]{
                        String.valueOf(idCounter),
                        URLDecoder.decode(m.get("name"), "UTF-8"),
                        m.get("faculty"),
                        m.get("prn"),
                        m.get("email"),
                        m.getOrDefault("devops","0"),
                        m.getOrDefault("java","0"),
                        m.getOrDefault("python","0"),
                        m.getOrDefault("aws","0"),
                        m.getOrDefault("android","0")
                });

                idCounter++;

                t.getResponseHeaders().add("Location","/view");
                t.sendResponseHeaders(302,-1);
            }
        }
    }

    // ✅ UPDATED VIEW (GRAPH + DASHBOARD LOOK)
    static class ViewStudents implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {

            String res = "<html>"+style+"<body><h2>STUDENT RECORDS</h2>";

            for(String[] s:students){

                res += "<div class='card'>" +
                        "<b>Name:</b> "+s[1]+"<br>" +
                        "<b>Faculty:</b> "+s[2]+"<br>" +
                        "<b>PRN:</b> "+s[3]+"<br>" +
                        "<b>Email:</b> "+s[4]+"<br><br>" +

                        "<b>Marks:</b><br>" +
                        "DevOps: "+s[5]+" | Java: "+s[6]+" | Python: "+s[7]+" | AWS: "+s[8]+" | Android: "+s[9]+"<br><br>" +

                        // GRAPH
                        "<canvas id='chart"+s[0]+"' height='200'></canvas>" +

                        "<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>" +
                        "<script>" +
                        "new Chart(document.getElementById('chart"+s[0]+"'),{" +
                        "type:'bar'," +
                        "data:{labels:['DevOps','Java','Python','AWS','Android']," +
                        "datasets:[{label:'Marks',data:["+
                        s[5]+","+s[6]+","+s[7]+","+s[8]+","+s[9]+"]}]}" +
                        "});" +
                        "</script>" +

                        "<br><a href='/update?id="+s[0]+"'><button class='btn update'>Update</button></a>" +
                        "<a href='/delete?id="+s[0]+"'><button class='btn delete'>Delete</button></a>" +
                        "</div>";
            }

            res += "<a href='/dashboard'><button>Back</button></a></body></html>";
            send(t,res);
        }
    }

    // UPDATE (same logic)
    static class UpdateStudent implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String q = t.getRequestURI().getQuery();

            if(q.contains("name")){
                Map<String,String> m = map(q);

                for(int i=0;i<students.size();i++){
                    if(students.get(i)[0].equals(m.get("id"))){
                        students.set(i,new String[]{
                                m.get("id"),
                                URLDecoder.decode(m.get("name"),"UTF-8"),
                                m.get("faculty"),
                                m.get("prn"),
                                m.get("email"),
                                m.get("devops"),
                                m.get("java"),
                                m.get("python"),
                                m.get("aws"),
                                m.get("android")
                        });
                    }
                }

                t.getResponseHeaders().add("Location","/view");
                t.sendResponseHeaders(302,-1);
                return;
            }

            Map<String,String> m = map(q);
            String[] s = students.stream().filter(x->x[0].equals(m.get("id")))
                    .findFirst().orElse(new String[10]);

            send(t,"<html>"+style+"<body><div class='box'><h2>Update</h2>" +
                    "<form action='/update'>" +
                    "<input type='hidden' name='id' value='"+m.get("id")+"'>" +
                    "<input name='name' value='"+s[1]+"'>" +
                    "<input name='faculty' value='"+s[2]+"'>" +
                    "<input name='prn' value='"+s[3]+"'>" +
                    "<input name='email' value='"+s[4]+"'>" +
                    "<button>Update</button></form></div></body></html>");
        }
    }

    static class Review implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            send(t,"<html>"+style+"<body><div class='box'><h2>Review</h2>" +
                    "<textarea placeholder='Feedback'></textarea>" +
                    "<button>Submit</button><h3>Thank You!</h3>" +
                    "<a href='/dashboard'><button>Back</button></a></div></body></html>");
        }
    }

    static class DeleteStudent implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String id = map(t.getRequestURI().getQuery()).get("id");
            students.removeIf(s->s[0].equals(id));
            t.getResponseHeaders().add("Location","/view");
            t.sendResponseHeaders(302,-1);
        }
    }

    static void send(HttpExchange t,String res)throws IOException{
        t.sendResponseHeaders(200,res.length());
        t.getResponseBody().write(res.getBytes());
        t.close();
    }

    static Map<String,String> map(String q){
        Map<String,String> m=new HashMap<>();
        if(q==null)return m;
        for(String p:q.split("&")){
            String[] a=p.split("=");
            m.put(a[0],a.length>1?a[1]:"");
        }
        return m;
    }
}