(ns metaballs
  (:import
    [javax.swing JFrame]
    [java.awt Canvas Graphics Color]
    java.awt.image.BufferStrategy))

(def  SIZE 300)
(def  THRESHOLD 1.005)

(defn move [{:keys [x y vx vy radius color]}]
  (let [vx (if (or (> x SIZE) (neg? x)) (- vx) vx)
        vy (if (or (> y SIZE) (neg? y)) (- vy) vy)]
    {:x (+ x vx)
     :y (+ y vy)
     :vx vx
     :vy vy
     :radius radius
     :color color}))

(defn fix-color [c]
  (cond 
    (< c 0) 0
    (> c 255) 255
    :default c))

(defn color-in-range [r g b]
  (new Color (int (fix-color r)) (int (fix-color g)) (int (fix-color b))))

(defn influence 
  [{:keys [x y radius]} px py]
  (let [dx (double (- x px))
        dy (double (- y py))]
    (double (/ radius (Math/sqrt (+ (* dx dx) (* dy dy)))))))

(defn paint-square [^Graphics g ^Color color x y size]
  (doto g
    (.setColor color)
    (.fillRect x y size size)))

(defn compute-color [x y [sum red-cur green-cur blue-cur] ball]   
  (let [influence (influence ball x y)
        [r g b] (:color ball)] 
    [(+ sum influence)
     (+ red-cur (* influence r))
     (+ green-cur (* influence g))
     (+ blue-cur (* influence b))]))

(defn draw [^Canvas canvas balls]
  (let [buffer (.getBufferStrategy canvas)
        g      (.getDrawGraphics buffer)
        step   3]
    (try
      (loop [x 0]
        (loop [y 0]          
          (let [[total red green blue] 
                (reduce (partial compute-color x y) [0 0 0 0] balls)]                        
            (paint-square g (color-in-range red green blue) x y step))            
          (if (< y SIZE) (recur (int (+ y step)))))
        (if (< x SIZE) (recur (int (+ x step)))))
      
      (finally (.dispose g)))
    (if-not (.contentsLost buffer)
      (.show buffer)) ))
 
(defn metaball [_]
  {:x      (rand-int SIZE)
   :y      (rand-int SIZE)
   :vx     (double (inc (rand-int 6)))
   :vy     (double (inc (rand-int 6)))
   :radius (+ 10 (rand-int 19))
   :color  [(rand-int 256) (rand-int 256) (rand-int 256)]})

(defn -main [& args]
  (let [frame  (JFrame. "Metaballs")
        canvas (Canvas.)]
     
    (doto frame
      (.setSize SIZE SIZE)      
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setResizable false)
      (.add canvas)
      (.setVisible true))
 
    (doto canvas
      (.createBufferStrategy 2)      
      (.setVisible true)
      (.requestFocus))
         
    (loop [balls (map metaball (range 6))]      
      (draw canvas balls)
      (recur (map move balls)))))
 
(-main)