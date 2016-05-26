close all

% X1 = importdata('loon/outputData/windlayer1_X.txt');
% Y1 = importdata('loon/outputData/windlayer1_Y.txt');
% 
% X2 = importdata('loon/outputData/windlayer2_X.txt');
% Y2 = importdata('loon/outputData/windlayer2_Y.txt');
% 
% X3 = importdata('loon/outputData/windlayer3_X.txt');
% Y3 = importdata('loon/outputData/windlayer3_Y.txt');
% 
% X4 = importdata('loon/outputData/windlayer4_X.txt');
% Y4 = importdata('loon/outputData/windlayer4_Y.txt');



%coverage = importdata('loon/simulation_coverage2');
% h = figure;
% subplot(2,2,1)
% quiver(X1,Y1)
% xlim([1 50])
% ylim([1 50])
% title('Windlayer 1')
% subplot(2,2,2)
% quiver(X2,Y2)
% xlim([1 25])
% ylim([1 25])
% title('Windlayer 2')
% subplot(2,2,3)
% quiver(X3,Y3)
% xlim([1 25])
% ylim([1 25])
% title('Windlayer 3')
% subplot(2,2,4)
% quiver(X4,Y4)
% xlim([1 25])
% ylim([1 25])
% title('Windlayer 4')
%print -painters -dpdf -r600 test.pdf


sim_cov_alg1 = importdata('loon/outputData/simulation_coverage_alg1.txt');
sim_cov_alg2 = importdata('loon/outputData/simulation_coverage_alg2.txt');
sim_cov_alg3 = importdata('loon/outputData/simulation_coverage_alg3.txt');
sim_cov_alg3s = importdata('loon/outputData/simulation_coverage_alg3s.txt');
sim_cov_alg4 = importdata('loon/outputData/simulation_coverage_alg4.txt');
sim_cov_alg4s = importdata('loon/outputData/simulation_coverage_alg4s.txt');
% 
% 

steps = 5000;
figure
% subplot(1,2,1)
% plot(sim_cov_alg1(:,1), sim_cov_alg1(:,2))
% title('Algorithm 1')
% xlabel('t')
% ylabel('Coverage')
% xlim([0 steps])
% ylim([0 1])
% 
% subplot(1,2,2)
% plot(sim_cov_alg2(:,1), sim_cov_alg2(:,2))
% title('Algorithm 2')
% xlabel('t')
% ylabel('Coverage')
% xlim([0 steps])
% ylim([0 1])

%  subplot(1,2,1)
%  plot(sim_cov_alg3(:,1), sim_cov_alg3(:,2))
% title('Algorithm 3')
% xlabel('t')
% ylabel('Coverage')
% xlim([0 steps])
% ylim([0 1])
% 
%  subplot(1,2,2)
%  plot(sim_cov_alg3s(:,1), sim_cov_alg3s(:,2))
% title('Algorithm 3s')
% xlabel('t')
%  ylabel('Coverage')
%  xlim([0 steps])
%  ylim([0 1])
 
%  
 subplot(1,2,1)
 plot(sim_cov_alg4(:,1), sim_cov_alg4(:,2))
title('Algorithm 4')
xlabel('t')
 ylabel('Coverage')
 xlim([0 steps])
 ylim([0 1])
 
 subplot(1,2,2)
 plot(sim_cov_alg4s(:,1), sim_cov_alg4s(:,2))
title('Algorithm 4s')
xlabel('t')
 ylabel('Coverage')
 xlim([0 steps])
 ylim([0 1])

%print('report/graphics/coverage_alg4_vs_alg4s_1000_long','-dpdf')
% %
% % 
% figure
% plot(sim_cov_alg3_no_delay(:,1),sim_cov_alg3_no_delay(:,2))
%  heatmap = importdata('loon/Simulation_Heatmap.txt');
% HeatMap(heatmap,'redgreencmap')
%  figureclc
%  surf(heatmap)
%  colormap flag
% 
% heatmap2 = importdata('loon/Simulation_Heatmap_alg2.txt');
% %HeatMap(heatmap,'redgreencmap')
% figure
% surf(heatmap2)
% colormap flag
