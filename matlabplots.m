 close all

X1 = importdata('loon/outputData/windlayer1_X.txt');
Y1 = importdata('loon/outputData/windlayer1_Y.txt');

X2 = importdata('loon/outputData/windlayer2_X.txt');
Y2 = importdata('loon/outputData/windlayer2_Y.txt');

X3 = importdata('loon/outputData/windlayer3_X.txt');
Y3 = importdata('loon/outputData/windlayer3_Y.txt');

X4 = importdata('loon/outputData/windlayer4_X.txt');
Y4 = importdata('loon/outputData/windlayer4_Y.txt');



%coverage = importdata('loon/simulation_coverage2');
h = figure;
subplot(2,2,1)
quiver(X1,Y1)
xlim([1 25])
ylim([1 25])
title('Windlayer 1')
subplot(2,2,2)
quiver(X2,Y2)
xlim([1 25])
ylim([1 25])
title('Windlayer 2')
subplot(2,2,3)
quiver(X3,Y3)
xlim([1 25])
ylim([1 25])
title('Windlayer 3')
subplot(2,2,4)
quiver(X4,Y4)
xlim([1 25])
ylim([1 25])
title('Windlayer 4')
print('report/graphics/windlayers','-dpdf')
print -painters -dpdf -r600 test.pdf


% sim_cov_alg1 = importdata('loon/outputData/simulation_coverage_alg1.txt');
% sim_cov_alg2 = importdata('loon/outputData/simulation_coverage_alg2.txt');
% sim_cov_alg3 = importdata('loon/outputData/simulation_coverage_alg3.txt');
% sim_cov_alg3s = importdata('loon/outputData/simulation_coverage_alg3s.txt');
% sim_cov_alg4 = importdata('loon/outputData/simulation_coverage_alg4.txt');
% sim_cov_alg4s = importdata('loon/outputData/simulation_coverage_alg4s.txt');
% 
% 
% sim_cov_alg4_6 = importdata('loon/outputData/archive/simulation_coverage_alg4_6.txt');
% sim_cov_alg4_2 = importdata('loon/outputData/archive/simulation_coverage_alg4_2.txt');
% sim_cov_alg4_3 = importdata('loon/outputData/archive/simulation_coverage_alg4_3.txt');
% sim_cov_alg4_4 = importdata('loon/outputData/archive/simulation_coverage_alg4_4.txt');
sim_cov_alg4_10 = importdata('loon/outputData/archive/simulation_coverage_alg4_10.txt');
sim_cov_alg4_10_easy = importdata('loon/outputData/archive/simulation_coverage_alg4_10_easy.txt');

% % 
% % 
% 

figure


subplot(1,2,1)
plot(sim_cov_alg4_10(:,1), sim_cov_alg4_10(:,2))
title('10x - Algorithm 4 ')
xlabel('t')
ylabel('Coverage')
xlim([0 steps])
ylim([0 1])
% 
%  subplot(2,2,2)
%  plot(sim_cov_alg4_3(:,1), sim_cov_alg4_3(:,2))
% title('3x')
% xlabel('t')
% ylabel('Coverage')
% xlim([0 steps])
% ylim([0 1])

%  subplot(2,2,3)
% plot(sim_cov_alg4_4(:,1), sim_cov_alg4_4(:,2))
% title('4x')
% xlabel('t')
%  ylabel('Coverage')
%  xlim([0 steps])
%  ylim([0 1])
%  
%  subplot(2,2,3)
% plot(sim_cov_alg4_4(:,1), sim_cov_alg4_4(:,2))
% title('4x')
% xlabel('t')
%  ylabel('Coverage')
%  xlim([0 steps])
%  ylim([0 1])
 
 
 subplot(1,2,2)
plot(sim_cov_alg4_10_easy(:,1), sim_cov_alg4_10_easy(:,2))
title('10x - Algorithm 4 - uniform wind')
xlabel('t')
 ylabel('Coverage')
 xlim([0 steps])
 ylim([0 1])
  % print('report/graphics/alg4_lifetime200_scaling','-dpdf')
%  
%  
%  subplot(1,2,1)
%  plot(sim_cov_alg4(:,1), sim_cov_alg4(:,2))
% title('Algorithm 4')
% xlabel('t')
%  ylabel('Coverage')
%  xlim([0 steps])
%  ylim([0 1])
%  
%  subplot(1,2,2)
%  plot(sim_cov_alg4s(:,1), sim_cov_alg4s(:,2))
% title('Algorithm 4s')
% xlabel('t')
%  ylabel('Coverage')
%  xlim([0 steps])
%  ylim([0 1])
%print('report/graphics/alg4vsalg4s_2000_steps_lifetime_LONG','-dpdf')
 %print('report/graphics/coverage_alg4_vs_alg4s_3000_200','-dpdf')
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

