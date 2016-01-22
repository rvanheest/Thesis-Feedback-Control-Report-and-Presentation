module ObservableMonad where

newtype Observable t = Obs { runObs :: (t -> ()) -> () }

instance Monad Observable where 
    return t = Obs $ \c -> c t
    (Obs f) >>= g = Obs $ \c -> f (\a -> let (Obs b) = g a in b c)