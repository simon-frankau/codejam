-- Round 3, 2012, complete

module Main where

import Control.Applicative
import Control.Monad
import Data.List
import Debug.Trace

calculate :: [(Rational, Rational)] -> [Integer]
calculate cases = res where
  -- l is seconds it lasts, p is chance of success
  -- given x seconds expected to reach this point,
  -- expected time to pass here is (x + l)/(1-p).
  -- rewrite as ax + b, a = 1/p, b = l/p.
  cases' = map (\(l, p) -> (100/(100-p), 100*l/(100-p)))
               cases
  -- Do 1 then 2 if
  --  a_2 (a_1 x + b_1) + b_2 < a_1 (a_2 x + b_2) + b_1
  --  a_1 a_2 x + a_2 b_1 + b2 < a_1 a_2 x + a_1 b_2 + b_1
  --  a_2 b_1 + b_2 < a_1 b_2 + b_1
  --  (a_2 - 1) b_1 < (a_1 - 1) b_2
  --  b_1 / (a_1 - 1) < b_2 / (a_2 - 1)
  cases'' = zip cases' [0..]
  end_stuff   = filter (\((a, _b), _) -> a == 1) cases''
  start_stuff = filter (\((a, _b), _) -> a /= 1) cases''

  scores = map (\((a, b), idx) -> (b / (a - 1), idx)) start_stuff
  res = map snd (sort scores) ++ map snd end_stuff

runCase i = do
  _n <- read <$> getLine :: IO Integer
  ls <- map read . words <$> getLine :: IO [Integer]
  ps <- map read . words <$> getLine :: IO [Integer]
  let ls' = map fromIntegral ls
  let ps' = map fromIntegral ps
  let xs = calculate $ zip ls' ps'
  putStrLn $ "Case #" ++ show i ++ ": " ++
             (intercalate " " $ map show xs)

main = do
  t <- read <$> getLine
  mapM_ runCase [1..t]
